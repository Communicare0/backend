package com.example.backend.dto;

import com.example.backend.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {

    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 4, max = 255, message = "비밀번호는 4자 이상 255자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 항목입니다.")
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    private String nickname;

    @Size(max = 100, message = "학과는 100자 이하여야 합니다.")
    private String department;

    @Size(max = 20, message = "학번은 20자 이하여야 합니다.")
    private String studentId;

    @Size(max = 50, message = "국적은 50자 이하여야 합니다.")
    private String nationality;

    @Size(max = 10, message = "언어는 10자 이하여야 합니다.")
    private String language;

    @Size(max = 2048, message = "프로필 이미지 URL은 2048자 이하여야 합니다.")
    private String profileImageUrl;

    @Builder.Default
    private UserRole role = UserRole.USER;
}