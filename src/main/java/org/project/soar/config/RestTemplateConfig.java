package org.project.soar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("${openai.secret-key}")
    private String openAISecretKey;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(100000);  // 연결 타임아웃 (ms)
        factory.setReadTimeout(600000);     // 응답 대기 타임아웃 (ms)
        return new RestTemplate(factory);
    }

    //일반 api 호출용
    @Bean
    public HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    //gpt api 호출용
    @Bean
    public HttpHeaders gptHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAISecretKey);  // GPT 인증용
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}
