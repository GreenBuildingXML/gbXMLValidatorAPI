package com.bimport.ashrae.mySQL.user.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserByUsername(String username);
    User findFirstByHashUsernameAndIsActive(String hash, Boolean isActive);
    Optional<User> findById(Long id);
}
