package com.example.springsecurityexample.match.controller;

import com.example.springsecurityexample.match.*;
import com.example.springsecurityexample.match.Error;
import com.example.springsecurityexample.match.dto.*;
import com.example.springsecurityexample.match.repository.MatchRepository;
import com.example.springsecurityexample.match.service.MatchService;
import com.example.springsecurityexample.member.Member;
import com.example.springsecurityexample.security.CustomUserDetails;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

//TODO : JPA 설정 관련 WARNING LOG 해결
//TODO : URL 규칙에 맞춰서 수정 & service와 controller 분리, link 추가, docs 추가
@Controller
@RequiredArgsConstructor //DI
@RequestMapping(value = "/api", produces = MediaTypes.HAL_JSON_VALUE)
public class MatchController {

    private final MatchRepository matchRepository;

    private final MatchService matchService;

    private final ModelMapper modelMapper;

    // 이 코드 여기 있으면 안될것 같음
    Long GetLoginUserId () {
        Long id;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Member member = userDetails.getMember();
            id = member.getId();
            return id;
        }
        else return null;
    }

    @ApiOperation(
            value = "매칭 생성"
            , notes = "json을 통해 받은 사용자의 ID와 원하는 점수범위를 통해 매칭을 생성한다.\n" +
            "참고 : 샘플 데이터가 없어 현재는 0~100 사이 랜덤한 uid2를 생성합니다.")
    @PostMapping("/post/match")
    public ResponseEntity CreateMatch(@RequestBody MatchRequestDto matchRequestDto/*, 기술 스택 dto 파라미터*/) {
        //TODO : 에러 관련 코드 추가(하는 중)
        //TODO : 필터링 기능 (후 순위)


        if (matchRequestDto.getUid1() == null) { //uid1 body에 담아서 요청했는지 확인
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Error(false, "Uid1 parameter is null in body"));
        }
        if (!Objects.equals(matchRequestDto.getUid1(), GetLoginUserId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Error(
                            false,
                            "다른 사용자의 매칭을 만들 수 없습니다. 현재 사용자의 id를 파라미터로 보내주세요"
                    ));
        }

        if(matchService.RecommendUser(matchRequestDto) == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Error(false, "더 이상 만들 수 있는 매칭 유져가 없습니다." +
                            "(추후 추가 예정임) 추천할 유저의 점수 범위를 늘려주세요."));

//        if(memberRepository.findById(userId) == null) //uid1이 존재하는 유저인지 확인/ member table 필요
//        {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new DeleteResultResponse(false, "uid1 is not founded"));
//        }



        MatchResponseDto newMatch = matchService.RecommendUser(matchRequestDto);
        Match match = modelMapper.map(newMatch, Match.class);
        Match currentMatch = matchRepository.save(match);

        var selfLinkBuilder = linkTo(methodOn(MatchController.class).CreateMatch(matchRequestDto));
        URI createdUri = selfLinkBuilder.toUri();
        var getUserMatchesLinkBuilder = linkTo(methodOn(MatchController.class).ReadUserMatch(currentMatch.getUid1()));
        var updateDeleteLinkBuilder = linkTo(methodOn(MatchController.class).DeleteMatch(currentMatch.getMatchId()));
        var updateSuccessLinkBuilder = linkTo(methodOn(MatchController.class).UpdateMatchStatus(currentMatch.getMatchId()));
        var updateLikeLinkBuilder = linkTo(methodOn(MatchController.class).LikeMatch(currentMatch.getMatchId()));

        MatchResource matchResource = new MatchResource(match);
        matchResource.add(selfLinkBuilder.withSelfRel());
        matchResource.add(getUserMatchesLinkBuilder.withRel("get user's matching info list(get)"));
        matchResource.add(updateDeleteLinkBuilder.withRel("delete failure match(delete)"));
        matchResource.add(updateSuccessLinkBuilder.withRel("update successful match(patch)"));
        matchResource.add(updateLikeLinkBuilder.withRel("like to another user(patch)"));

        return ResponseEntity.created(createdUri).body(matchResource);
    }

    @ApiOperation(
            value = "사용자 매칭 조회"
            , notes = "URI를 통해 받은 사용자의 ID로 사용자의 매칭 리스트를 조회한다.")
    @GetMapping("/get/match/{id}")
    public ResponseEntity ReadUserMatch(@PathVariable Long id) { //계정 매개변수 있어야하나?
        List<Match> matchUsers = matchService.GetMatchingList(id);
        if (matchUsers.isEmpty()) { //TODO:서비스에서 처리해야 의미있는 코드가 됨.
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Error(false, "uid1's matching is not founded"));
        }
        return ResponseEntity.ok(matchUsers);
    }

    @ApiOperation(
            value = "매칭 삭제"
            , notes = "URI를 통해 받은 매칭 ID로 매칭을 삭제한다. " +
            "\n매칭이 성사되고 팀이 만들어진 후 또는 매칭을 거절하는 경우 사용 권장")
    @DeleteMapping("/delete/match/{matchId}")
    public ResponseEntity DeleteMatch(@PathVariable Long matchId){
        //TODO : 트렌젝션 관련 오류 처리 공부
        try {
            matchRepository.deleteById(matchId);
            return ResponseEntity
                    .ok(new Error(true, "success"));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new Error(false, "Resource not found"));
        }
    }

    @ApiOperation(
            value = "매칭 성사"
            , notes = "URI를 통해 받은 매칭 ID로 매칭 성사 여부를 저장하는 칼럼을 true로 변경" +
            "\n매칭 성사 시 사용 권장.")
    @PatchMapping("/patch/match/success/{matchId}")
    public ResponseEntity UpdateMatchStatus(@PathVariable Long matchId){
        Optional<Match> matchOptional = matchRepository.findById(matchId);

        if (matchOptional.isPresent()) {
            Match match = matchOptional.get();
            match.setMatchSuccess(true);
            // Dto를 사용하여 업데이트 해야함. (현재는 entity를 사용해서 데이터를 옮김)
            matchRepository.save(match);
            return ResponseEntity.ok(match); // 변경된 Match 객체를 반환
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Error(false, "Matching not found"));
        }
    }

    @ApiOperation(
            value = "매칭 요청"
            , notes = "URI를 통해 받은 매칭 ID로 매칭 요청 여부를 저장하는 칼럼을 true로 변경" +
            "\n매칭 요청 시 사용 권장.")
    @PatchMapping("/patch/match/like/{matchId}")
    public ResponseEntity LikeMatch(@PathVariable Long matchId){
        Optional<Match> matchOptional = matchRepository.findById(matchId);

        if (matchOptional.isPresent()) {
            Match match = matchOptional.get();
            match.setMatchingRequest(true);
            // updatedMatchDto를 사용하여 업데이트 해야함. (현재는 entity를 사용해서 데이터를 옮김)
            matchRepository.save(match);
            return ResponseEntity.ok(match); // 변경된 Match 객체를 반환
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Error(false, "Matching not found"));
        }
    }
}

