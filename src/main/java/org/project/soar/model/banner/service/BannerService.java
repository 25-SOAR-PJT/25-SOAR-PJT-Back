package org.project.soar.model.banner.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.banner.Banner;
import org.project.soar.model.banner.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final BannerRepository bannerRepository;

    public Banner saveBanner(MultipartFile file, String url) throws IOException {
        Banner banner = Banner.builder()
                .data(file.getBytes())
                .contentType(file.getContentType())  // MIME 타입 저장
                .url(url) // url 함께 저장
                .build();
        return bannerRepository.save(banner);
    }

    public Banner getBanner(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 배너가 존재하지 않습니다: " + id));
    }
}
