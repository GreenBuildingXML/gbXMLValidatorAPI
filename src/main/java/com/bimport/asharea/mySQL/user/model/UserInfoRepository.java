package com.bimport.asharea.mySQL.user.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    UserInfo findTopByEmail(String email);
    Optional<UserInfo> findById(Long id);
}
