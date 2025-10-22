package com.example.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRequest {
    @NotBlank(message = "문장을 입력해주세요.")
    @Size(max = 1000, message = "문장은 1000자를 초과할 수 없습니다.")
    private String sentence;
}
