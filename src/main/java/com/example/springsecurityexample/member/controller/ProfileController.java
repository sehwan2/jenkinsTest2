package com.example.springsecurityexample.member.controller;

import com.example.springsecurityexample.member.Profile;
import com.example.springsecurityexample.member.dto.ProfileRequest;
import com.example.springsecurityexample.member.dto.ProfileResponse;
import com.example.springsecurityexample.member.dto.SignResponse;
import com.example.springsecurityexample.member.repository.ProfileRepository;
import com.example.springsecurityexample.member.service.ProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/profile/{memberId}")
    @ApiOperation("memberId에 해당하는 프로필 정보를 저장")
    public ResponseEntity<Profile> createOrUpdateProfile(@PathVariable Long memberId, @RequestBody ProfileRequest profileRequest) {
        Profile profile = profileService.UpdateProfile(memberId, profileRequest);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @GetMapping("/profile/{memberId}")
    @ApiOperation("memberId의 프로필 정보 가져옴")
    public ResponseEntity<Profile> getProfileByMemberId(@PathVariable Long memberId) {
        Profile profile = profileService.getProfileByMemberId(memberId);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @GetMapping("/profile/{content}/{id}")
    @ApiOperation("프로필 정보를 하나만 수정가능")
    public ResponseEntity<String> getProfileContentById(@PathVariable String content, @PathVariable Long id) {
        Profile profile = profileService.getProfileByMemberId(id);

        if (profile == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String result = "";

        switch (content) {
            case "nickname":
                result = profile.getNickname();
                break;
            case "major":
                result = profile.getMember().getMajor();
                break;
            // 여기에 다른 원하는 정보에 대한 case를 추가할 수 있습니다.

            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}