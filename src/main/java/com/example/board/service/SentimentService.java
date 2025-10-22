package com.example.board.service;

import com.example.board.dto.SentimentRequest;
import com.example.board.dto.SentimentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentService {

    private final WebClient webClient;

    @Value("${sentiment.api.url:http://localhost:8088}")
    private String apiBaseUrl;

    @Value("${sentiment.api.token:05ac3793-8a82-4e5e-9e24-b084a77042b7}")
    private String apiToken;

    public SentimentResponse classifySentiment(SentimentRequest request) {
        try {
            log.info("Sentiment classification request for sentence: {}", request.getSentence());
            
            // 먼저 원시 응답을 받아서 로그로 확인
            String rawResponse = webClient
                    .post()
                    .uri(apiBaseUrl + "/classification/sentiment")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Raw API response: {}", rawResponse);
            
            // 원시 응답을 파싱하여 SentimentResponse로 변환
            SentimentResponse response = parseApiResponse(rawResponse);
            
            log.info("Parsed sentiment classification response: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            log.error("API 호출 실패 - Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return SentimentResponse.builder()
                    .result("ERROR")
                    .confidence("0.0")
                    .message("API 호출 실패: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생", e);
            return SentimentResponse.builder()
                    .result("ERROR")
                    .confidence("0.0")
                    .message("서버 오류: " + e.getMessage())
                    .build();
        }
    }
    
    private SentimentResponse parseApiResponse(String rawResponse) {
        try {
            // JSON 파싱을 위한 ObjectMapper 사용
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            
            // 배열 형태의 응답 처리
            com.fasterxml.jackson.databind.JsonNode jsonArray = mapper.readTree(rawResponse);
            
            if (jsonArray.isArray() && jsonArray.size() > 0) {
                // 가장 높은 확률을 가진 결과 찾기
                String bestResult = "UNKNOWN";
                double bestProb = 0.0;
                
                for (com.fasterxml.jackson.databind.JsonNode item : jsonArray) {
                    if (item.has("template") && item.has("prob")) {
                        String template = item.get("template").asText();
                        double prob = item.get("prob").asDouble();
                        
                        if (prob > bestProb) {
                            bestProb = prob;
                            bestResult = template;
                        }
                    }
                }
                
                return SentimentResponse.builder()
                        .result(bestResult)
                        .confidence(String.format("%.4f", bestProb))
                        .message("감정 분석 완료 - " + bestResult + " (" + String.format("%.2f", bestProb * 100) + "%)")
                        .build();
            }
        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
            return SentimentResponse.builder()
                    .result("ERROR")
                    .confidence("0.0")
                    .message("응답 파싱 오류: " + e.getMessage())
                    .build();
        }
        
        return SentimentResponse.builder()
                .result("ERROR")
                .confidence("0.0")
                .message("빈 응답 또는 잘못된 응답 형태")
                .build();
    }
}
