package com.example.board.controller;

import com.example.board.dto.SentimentRequest;
import com.example.board.dto.SentimentResponse;
import com.example.board.service.SentimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/sentiment")
@RequiredArgsConstructor
@Slf4j
public class SentimentController {

    private final SentimentService sentimentService;

    @GetMapping
    public String sentimentForm(Model model) {
        model.addAttribute("sentimentRequest", new SentimentRequest());
        return "sentiment/form";
    }

    @PostMapping("/analyze")
    public String analyzeSentiment(@Valid @ModelAttribute SentimentRequest request, Model model) {
        log.info("Received sentiment analysis request: {}", request.getSentence());
        
        try {
            SentimentResponse response = sentimentService.classifySentiment(request);
            model.addAttribute("sentimentResponse", response);
            model.addAttribute("originalSentence", request.getSentence());
        } catch (Exception e) {
            log.error("Error processing sentiment analysis request", e);
            SentimentResponse errorResponse = SentimentResponse.builder()
                    .result("ERROR")
                    .confidence("0.0")
                    .message("처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
            model.addAttribute("sentimentResponse", errorResponse);
            model.addAttribute("originalSentence", request.getSentence());
        }
        
        return "sentiment/result";
    }

    @PostMapping("/classification")
    @ResponseBody
    public ResponseEntity<SentimentResponse> classifySentiment(@Valid @RequestBody SentimentRequest request) {
        log.info("Received sentiment classification request: {}", request.getSentence());
        
        try {
            SentimentResponse response = sentimentService.classifySentiment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing sentiment classification request", e);
            SentimentResponse errorResponse = SentimentResponse.builder()
                    .result("ERROR")
                    .confidence("0.0")
                    .message("처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Sentiment Classification API is running");
    }
}
