package com.example.springsecurityexample.match.service;

import com.example.springsecurityexample.match.Match;
import com.example.springsecurityexample.match.dto.*;
import com.example.springsecurityexample.match.repository.MatchRepository;
import com.example.springsecurityexample.member.Member;
import com.example.springsecurityexample.member.repository.MemberRepository;
import com.example.springsecurityexample.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor //DI
public class MatchService {
    private final MatchRepository matchRepository;
    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;




    public MatchResponseDto RecommendUser (MatchRequestDto matchRequestDto){
        Long randomUserId;

        long memberCount;
        memberCount = memberRepository.count();

        Match match = modelMapper.map(matchRequestDto, Match.class);

        //유저 table이 완성되면 TeamRepository로 총 사용자 수를 랜덤의 최댓값을 바꾸자.(countby 사용)

//        do {
//            list<evalution> userScore = evalutionRepository.findAllBy평가총점(프론트에서 부탁한 점수);
//            if(userScore.isEmpty()) // 해당 점수대의 유저가 없음
//                return null;
//
//            recommenedUser = (long) (Math.random() * userScore.size());
//            match.setUid2(recommenedUser.getId());
//            //match 테이블에 없는 매칭일 경우 생성, 만약 매칭할 팀이 없다면? << 언제 루프를 탈출할지 정해야 함
//        } while (matchRepository.findAllByUid1AndUid2(match.getUid1(), recommenedUser.getId()) == null);

        do {
            randomUserId = (long) (Math.random() * 100 + 1);
            match.setUid2(randomUserId);
            //match 테이블에 없는 매칭일 경우 생성, 만약 매칭할 팀이 없다면? << 언제 루프를 탈출할지 정해야 함
        } while (matchRepository.findAllByUid1AndUid2(match.getUid1(), randomUserId) == null);

        match.setMatchingDate(LocalDateTime.now());
        match.setMatchSuccess(false);
        match.setMatchingRequest(false);
        match.update();

        MatchResponseDto matchResponseDto = modelMapper.map(match, MatchResponseDto.class);
        return matchResponseDto;

    }

    public List<Match> GetMatchingList (Long id){
        List<Match> matchList = matchRepository.findAllByUid1AndMatchingRequestIsFalse(id);
        //TODO : null exception 예외처리

            for (int i = 0; i < matchList.size(); i++) {
                matchList.get(i).update();
                if (matchList.get(i).getMatchingRequest() || matchList.get(i).getMatchSuccess()) {
                    matchList.remove(i);
                    i--;
                }
            }
        return matchList;
    }


}
