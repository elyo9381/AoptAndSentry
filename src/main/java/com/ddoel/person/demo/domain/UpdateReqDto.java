package com.ddoel.person.demo.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateReqDto {
    @NotBlank(message = "password를 입력하지 않았습니다.")
    private String password;
    private String phone;
}
