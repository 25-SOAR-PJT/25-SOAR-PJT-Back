package org.project.soar.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class YouthPolicyApiConfig {
    @Value("${youth.policy.api.key}")
    private String key;

    @Value("${youth.policy.api.base-url}")
    private String baseUrl;
}