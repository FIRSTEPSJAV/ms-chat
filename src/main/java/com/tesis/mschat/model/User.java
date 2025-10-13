package com.tesis.mschat.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {


    private Long id;


    private String username;


    private String password;


    private String email;

    private String name;


    private String phoneNumber;


    private String role;

    private LocalDateTime registrationTime;
}
