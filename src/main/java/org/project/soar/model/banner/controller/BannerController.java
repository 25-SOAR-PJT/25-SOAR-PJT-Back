package org.project.soar.model.banner.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.banner.Banner;
import org.project.soar.model.banner.service.BannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
@Slf4j
class BannerController {
    private final BannerService bannerService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadBanner(@RequestParam("file") MultipartFile file) {
        log.info("업로드 시도 - 파일: {}, 크기: {}", file != null ? file.getOriginalFilename() : "null", file != null ? file.getSize() : -1);
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<String>) ApiResponse.createError("파일이 비어있습니다."));
            }
            Banner saved = bannerService.saveBanner(file);
            return ResponseEntity.ok(ApiResponse.createSuccess("배너 업로드 완료 (ID: " + saved.getBannerId() + ")"));
        } catch (Exception e) {
            log.error("배너 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<String>) ApiResponse.createError("배너 업로드 실패: " + e.getMessage()));
        }
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<byte[]>> getBanner(@PathVariable Long id) {
//        Banner banner = bannerService.getBanner(id);
//        HttpHeaders headers = new HttpHeaders();
//
//        String contentType = banner.getContentType();
//        if (contentType != null) {
//            headers.setContentType(MediaType.parseMediaType(contentType));
//        } else {
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // fallback
//        }
//
//        ApiResponse<byte[]> response = ApiResponse.createSuccess(banner.getData());
//        return new ResponseEntity<>(response, headers, HttpStatus.OK);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> getBannerBase64(@PathVariable Long id) {
        Banner banner = bannerService.getBanner(id);

        String base64Image = Base64.getEncoder().encodeToString(banner.getData());

        ApiResponse<String> response = ApiResponse.createSuccess(base64Image);
        return ResponseEntity.ok(response); // Content-Type: application/json 자동 설정
    }

}
