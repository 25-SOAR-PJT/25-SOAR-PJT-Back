package org.project.soar.model.banner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BannerResponse {
    private String base64Image;
    private String url;
}
