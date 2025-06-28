package org.project.soar.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "youth.policy.api")
@Getter
@Setter
public class YouthPolicyApiConfig {
    private String key;
    private String baseUrl;
}
