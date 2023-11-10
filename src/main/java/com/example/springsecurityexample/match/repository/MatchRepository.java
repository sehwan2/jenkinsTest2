package com.example.springsecurityexample.match.repository;

import com.example.springsecurityexample.match.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//@Primary

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByUid1AndMatchingRequestIsFalse(Long Uid1);
    List<Match> findAllByUid1AndUid2(Long Uid1, Long Uid2);
}
