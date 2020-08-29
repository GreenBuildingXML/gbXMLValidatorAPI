package com.bimport.asharea.mySQL.user.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;
    private String phone;
    private String organization;
    private Boolean validated;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false)
    @JsonIgnore
    private User user;
    public String getFullname(){
        return firstName + " "  + lastName;
    }
    public UserInfo() {
    }
}
