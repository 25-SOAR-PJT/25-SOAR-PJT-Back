package org.project.soar.model.tag.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.RestTemplateConfig;
import org.project.soar.model.tag.YouthPolicyTag;
import org.project.soar.model.tag.dto.ChatCompletion;
import org.project.soar.model.tag.dto.PromptRequest;
import org.project.soar.model.tag.dto.YouthPolicyTagResponse;
import org.project.soar.model.tag.repository.TagRepository;
import org.project.soar.model.tag.repository.YouthPolicyTagRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.dto.YouthPolicyOpenAI;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.List.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGPTServiceImpl implements ChatGPTService{
    private final RestTemplateConfig restTemplateConfig;
    public final YouthPolicyRepository youthPolicyRepository;
    public final TagRepository tagRepository;
    public final YouthPolicyTagService youthPolicyTagService;
    public final YouthPolicyTagRepository youthPolicyTagRepository;

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
    public List<YouthPolicyTag> runPrompt() {
        List<YouthPolicy> top2YouthPolicies = youthPolicyRepository.findTop10ByOrderByCreatedAtDesc();
        List<YouthPolicyOpenAI> policyOpenAIs = top2YouthPolicies.stream()
                .map(youthPolicy -> new YouthPolicyOpenAI(
                        youthPolicy.getPolicyId(),
                        youthPolicy.getPolicyName(),
                        youthPolicy.getPolicyExplanation(),
                        youthPolicy.getPolicySupportContent(),
                        youthPolicy.getApplyMethodContent(),
                        youthPolicy.getScreeningMethodContent(),
                        youthPolicy.getSubmitDocumentContent(),
                        youthPolicy.getSupportTargetMinAge(),
                        youthPolicy.getSupportTargetMaxAge(),
                        youthPolicy.getSupportTargetAgeLimitYn()))
                .collect(Collectors.toList());

        HttpHeaders headers = restTemplateConfig.gptHeaders();
        List<YouthPolicyTag> resultList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (YouthPolicyOpenAI policy : policyOpenAIs) {
            if (youthPolicyTagRepository.existsByYouthPolicy(youthPolicyRepository.getById(policy.getPolicyId()))){
                //만약 이미 해당 정책에 태그가 존재한다면 skip
                log.info("이미 태깅된 정책입니다. {}",policy.getPolicyId());
                continue;
            }

            Map<String, String> inputItem = new HashMap<>();
            inputItem.put("role", "user");
            inputItem.put("content", generatePolicyInputText(policy));

            List<Map<String, String>> inputList = of(inputItem);

            PromptRequest requestDto = PromptRequest.builder()
                    .prompt(new PromptRequest.Prompt("pmpt_685a20474a2c8190adce753fb6276c590acaebac451f042b", "4"))
                    .input(inputList)
                    .reasoning(Collections.emptyMap())
                    .max_output_tokens(2048)
                    .store(true)
                    .build();

            HttpEntity<PromptRequest> entity = new HttpEntity<>(requestDto, headers);

            try {
                ResponseEntity<String> response = restTemplateConfig
                        .restTemplate()
                        .exchange(promptEndpoint, HttpMethod.POST, entity, String.class);

                log.info("[Raw 응답 JSON] {}", response.getBody());

                Map<String, Object> parsed = mapper.readValue(response.getBody(), new TypeReference<>() {
                });
                Object outputObj = parsed.get("output");

                if (outputObj instanceof List<?>) {
                    List<?> outputList = (List<?>) outputObj;
                    Map<String, Object> output = (Map<String, Object>) outputList.get(0);

                    Object tagIdObj = output.get("content");
                    if (tagIdObj instanceof List<?> contentList && !contentList.isEmpty()) {
                        Object first = contentList.get(0);
                        if (first instanceof Map<?, ?> contentMap) {
                            Object textObj = contentMap.get("text");
                            if (textObj instanceof String tagIdText) {
                                String cleaned = tagIdText.replaceAll("[^0-9,]", "");
                                List<Long> tagIds = Arrays.stream(cleaned.split(","))
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .map(Long::parseLong)
                                        .collect(Collectors.toList());

                                log.info("[Prompt 결과] policyId: {}, tagIds: {}", policy.getPolicyId(), tagIds);

                                for (Long tagId : tagIds) {
                                    YouthPolicyTagResponse tagResponse = new YouthPolicyTagResponse(policy.getPolicyId(), tagId);
                                    YouthPolicyTag result = youthPolicyTagService.setYouthPolicyTag(tagResponse);
                                    log.info("Saved YouthPolicyTag: policyId={}, tagId={}", policy.getPolicyId(), tagId);
                                    resultList.add(result);
                                }

                            } else {
                                log.warn("[경고] text가 문자열 형식이 아님: {}", textObj);
                            }
                        } else {
                            log.warn("[경고] content 항목이 예상한 Map 형태가 아님: {}", first);
                        }
                    } else {
                        log.warn("[경고] content가 리스트가 아니거나 비어 있음: {}", tagIdObj);
                    }
                } else {
                    log.warn("[경고] output이 리스트가 아님: {}", outputObj);
                }
            } catch (Exception e) {
                log.error("[에러] Prompt 호출 실패 - policyId: {}", policy.getPolicyId(), e);
            }
        }
        return resultList;
    }
    public String generatePolicyInputText(YouthPolicyOpenAI policy) {
        StringBuilder builder = new StringBuilder();

        Arrays.stream(YouthPolicyOpenAI.class.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            try {
                Object value = field.get(policy);
                builder.append(field.getName()).append(": ")
                        .append(value != null ? value.toString() : "")
                        .append("\n");
            } catch (IllegalAccessException e) {
                log.info("[에러] 필드 접근 실패: {}", field.getName(), e);
            }
        });

        return builder.toString();
    }
}

