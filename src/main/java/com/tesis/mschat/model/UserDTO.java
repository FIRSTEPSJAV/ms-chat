package com.tesis.mschat.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String phoneNumber;
    private String role;
    private LocalDateTime registrationTime;

    public static UserDTO fromEntity(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setRegistrationTime(user.getRegistrationTime());
        return dto;
    }
}