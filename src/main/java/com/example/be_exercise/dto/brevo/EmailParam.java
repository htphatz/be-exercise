package com.example.be_exercise.dto.brevo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailParam {
    private String email;
    private Object code;
}
