package com.bimport.ashrae.mySQL.software.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftwareRepo extends JpaRepository<Software, Long> {
    List<Software> findAllByUserId(String userId);
}
