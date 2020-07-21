package com.bimport.asharea.mySQL.user;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String hashUsername;

    private String password;

    private String salt;

    private Boolean isActive = false;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createdDate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private UserInfo userInfo;
    public User() {
    }

    public User(String username, String hashUsername, String password, String salt) {
        this.username = username;
        this.hashUsername = hashUsername;
        this.password = password;
        this.salt = salt;
    }
}
