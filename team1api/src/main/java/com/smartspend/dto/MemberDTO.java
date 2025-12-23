package com.smartspend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {

    private Long id;
    private String employeeNo;
    private String email;
    private String name;
    private String departmentName;
    private String role; // "USER" 또는 "ADMIN"
    private Boolean isActive;
}

