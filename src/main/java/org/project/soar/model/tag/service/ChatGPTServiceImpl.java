package org.project.soar.model.tag.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.RestTemplateConfig;
import org.project.soar.model.tag.dto.ChatCompletion;
import org.project.soar.model.tag.dto.PromptRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatGPTServiceImpl implements ChatGPTService{
    private final RestTemplateConfig restTemplateConfig;
    public ChatGPTServiceImpl(RestTemplateConfig restTemplateConfig) {
        this.restTemplateConfig = restTemplateConfig;
    }

    @Value("${openai.url.model}")
    private String modelUrl;

    @Value("${openai.url.model-list}")
    public String modelListUrl;

    @Value("${openai.url.prompt}")
    public String promptUrl;

    @Value("${openai.prompt-endpoint}")
    private String promptEndpoint;

    //사용가능한 모델 리스트 조회
    @Override
    public List<Map<String, Object>> modelList() {
        log.debug("모델 리스트를 조회합니다.");
        List<Map<String,Object>> resultList = null;

        // 토큰 정보가 포함된 Header 가져옴
        HttpHeaders headers = restTemplateConfig.gptHeaders();
        log.debug("요청 URL: " + modelUrl);
        // 통신을 위한 RestTemplate 구성
        ResponseEntity<String> response = restTemplateConfig
                .restTemplate()
                .exchange(modelUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        try {
            //Jackson을 기반으로 응답값 가져옴
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> data = om.readValue(response.getBody(), new TypeReference<>() {
            });

            //출력
            resultList = (List<Map<String, Object>>) data.get("data");
            for (Map<String, Object> object : resultList) {
                log.debug("ID: " + object.get("id"));
                log.debug("Object: " + object.get("object"));
                log.debug("Created: " + object.get("created"));
                log.debug("Owned By: " + object.get("owned_by"));
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            log.debug("RuntimeException: " + e.getMessage());
        }
        return resultList;
    }

    @Override
    public Map<String, Object> isValidModel(String modelName) {
        log.debug("모델이 유효한지 조회합니다. 모델 : "+modelName);
        Map<String, Object> result = new HashMap<>();

        //토큰 정보가 포함된 header 가져옴
        HttpHeaders headers = restTemplateConfig.gptHeaders();
        //통신을 위한 RestTemplate 객체 생성
        ResponseEntity<String> response = restTemplateConfig
                .restTemplate()
                .exchange(modelListUrl + "/" + modelName, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        try {
            // Jackson을 기반으로 응답값가져옴
            ObjectMapper om = new ObjectMapper();
            result = om.readValue(response.getBody(), new TypeReference<>() {
            });
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public Map<String, Object> prompt(ChatCompletion chatCompletion) {
        log.debug("신규 프롬프트 수행");
        Map<String, Object> resultMap = new HashMap<>();

        HttpHeaders headers = restTemplateConfig.gptHeaders();
        HttpEntity<ChatCompletion> requestEntity = new HttpEntity<>(chatCompletion, headers);
        ResponseEntity<String> response = restTemplateConfig
                .restTemplate()
                .exchange(promptUrl, HttpMethod.POST, requestEntity, String.class);
        try {
            ObjectMapper om = new ObjectMapper();
            resultMap = om.readValue(response.getBody(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e){
            log.debug("JsonMappingException :: " + e.getMessage());
        } catch (RuntimeException e){
            log.debug("RuntimeException :: " + e.getMessage());
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> runPrompt() {
        PromptRequest requestDto = PromptRequest.builder()
                .prompt(new PromptRequest.Prompt("pmpt_685a20474a2c8190adce753fb6276c590acaebac451f042b", "3"))
                .input(Collections.emptyList()) // 여기에 넣어야함
                .reasoning(Collections.emptyMap())
                .max_output_tokens(2048)
                .store(true)
                .build();

        HttpHeaders headers = restTemplateConfig.gptHeaders();
        HttpEntity<PromptRequest> entity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> response = restTemplateConfig
                .restTemplate()
                .exchange(promptEndpoint, HttpMethod.POST, entity, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.getBody(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
