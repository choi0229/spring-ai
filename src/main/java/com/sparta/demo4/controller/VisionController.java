package com.sparta.demo4.controller;

import com.sparta.demo4.controller.dto.ImageAnalysisResponse;
import com.sparta.demo4.service.ReceiptAnalysisService;
import com.sparta.demo4.service.VisionService;
import com.sparta.demo4.service.dto.ImageAnalysis;
import com.sparta.demo4.service.dto.ReceiptData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
public class VisionController {

    private final VisionService visionService;
    private final ReceiptAnalysisService receiptAnalysisService;

    /**
     * 이미지 분석(커스텀 프롬프트)
     * POST /api/vision/analyze
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageAnalysisResponse> analyzeImage(@RequestParam String prompt, @RequestParam MultipartFile image)throws IOException {
        ImageAnalysisResponse response = visionService.analyzeImage(ImageAnalysis.of(prompt, image));
        return ResponseEntity.ok(response);
    }

    /**
     * OCR - 이미지에서 텍스트 추출
     * POST /api/vision/ocr
     */
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> extractText(@RequestParam("image") MultipartFile image)throws IOException {
        String text = visionService.extractText(image);
        return ResponseEntity.ok(Map.of("text", text));
    }

    /**
     * 이미지 상세 설명
     * POST /api/vision/describe
     */
    @PostMapping(value = "describe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> describeImage(@RequestParam("image") MultipartFile image)throws IOException {
        String description = visionService.describeImage(image);
        return ResponseEntity.ok(Map.of("description", description));
    }

    /**
     * 차트/그래프 분석
     * POST /api/vision/describe
     */
    @PostMapping(value = "/chart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> analyzeChart(@RequestParam("image") MultipartFile image)throws IOException {
        String analysis = visionService.analyzeChart(image);
        return ResponseEntity.ok(Map.of("analysis", analysis));
    }

    /**
     * 두 이미지 비교
     * POST /api/vision/compare
     */
    @PostMapping(value = "/compare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> compareImage(@RequestParam("image") MultipartFile image1, @RequestParam("image") MultipartFile image2)throws IOException {
        String comparison = visionService.compareImages(image1, image2);
        return ResponseEntity.ok(Map.of("comparison", comparison));
    }

    @PostMapping(value = "/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReceiptData processReceipt(@RequestParam("image") MultipartFile image)throws IOException, NoSuchFieldException{
        return receiptAnalysisService.processReceipt(image);
    }
}
