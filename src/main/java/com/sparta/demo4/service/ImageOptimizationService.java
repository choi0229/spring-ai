package com.sparta.demo4.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class ImageOptimizationService {

    private static final int MAX_WIDTH = 1568;
    private static final int MAX_HEIGHT = 1568;
    private static final float JPEG_QUALITY = 0.85f;
    private static final int THUMBNAIL_SIZE = 200;

    /**
     * 영수증 이미지 최적화
     * - Claude Vision API 권장 해상도로 리사이징
     * - JPEG 품질 조정으로 용량 축소
     * - 메타데이터 제거
     */
    public byte[] optimize(MultipartFile imageFile) throws IOException {
        log.info("이미지 최적화 시작: {} ({} bytes)",
                imageFile.getOriginalFilename(),
                imageFile.getSize());

        // 1. 원본 이미지 읽기
        BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());

        if(originalImage == null){
            throw new IOException("이미지를 읽을 수 없습니다");
        }

        // 2. 리사이징 (필요한 경우에만)
        BufferedImage resized = resizeIfNeeded(originalImage);

        // 3. JPEG로 변환 + 춤질 조정
        byte[] optimized = compressAsJpeg(resized, JPEG_QUALITY);

        log.info("이미지 최적화 완료: {} bytes -> {} bytes({}% 감소)",
                imageFile.getSize(),
                optimized.length,
                (1 - (double) optimized.length / imageFile.getSize()) * 100);

        return optimized;
    }

    /**
     * 썸네일 생성
     */
    public byte[] createThumbnail(byte[] imageBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toOutputStream(baos);

        return baos.toByteArray();
    }

    /**
     * 필요한 경우에만 리사이징
     */
    private BufferedImage resizeIfNeeded(BufferedImage original) throws IOException {
        int width = original.getWidth();
        int height = original.getHeight();

        // 이미 작으면 리사이징 불필요
        if(width <= MAX_WIDTH && height <= MAX_HEIGHT){
            log.debug("리사이징 불필요: {}x{}", width, height);
            return original;
        }

        // 비율 유지하며 리사이징
        double ratio = Math.min(
                (double) MAX_WIDTH / width,
                (double) MAX_HEIGHT / height
        );

        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        log.debug("리사이징: {}x{} -> {}x{}", width, height, newWidth, newHeight);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thumbnails.of(original)
                .size(newWidth, newHeight)
                .outputFormat("jpg")
                .toOutputStream(baos);

        return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
    }

    /**
     * JPEG 압축(품질 조정)
     */
    private byte[] compressAsJpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // JPEG Writer 설정
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        writer.setOutput(ImageIO.createImageOutputStream(baos));
        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();

        return baos.toByteArray();
    }

    /**
     * 이미지 검증
     */
    public void validate(MultipartFile imageFile) {
        // 크기 검증(10MB)
        if(imageFile.getSize() > 10 * 1024 * 1024){
            throw new IllegalArgumentException("이미지 크기는 10MB를 초과할 수 없습니다");
        }

        // MIME 타입 검증
        String contentType = imageFile.getContentType();
        if(contentType == null || !contentType.startsWith("image/")){
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
        }

        // 확장자 검증
        String filename = imageFile.getOriginalFilename();
        if(filename == null || !filename.matches(".*\\.(jpg|jpeg|png|webp)")){
            throw new IllegalArgumentException("지원하는 형식: JPG, PNG, WEBP");
        }
    }
}
