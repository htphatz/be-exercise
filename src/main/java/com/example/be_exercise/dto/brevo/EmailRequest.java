package com.example.be_exercise.dto.brevo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EmailRequest {
    private List<RecipientRequest> to;
    private EmailParam params;
    private int templateId;
}
