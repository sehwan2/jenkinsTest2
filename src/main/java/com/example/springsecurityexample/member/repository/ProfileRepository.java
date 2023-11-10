package com.example.springsecurityexample.member.repository;

import com.example.springsecurityexample.member.Member;
import com.example.springsecurityexample.member.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByMember(Member member);
}
