package org.project.soar.model.tag.service;

import org.project.soar.model.tag.dto.ChatCompletion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface ChatGPTService {
    List<Map<String, Object>> modelList();
    Map<String, Object> isValidModel(String modelName);
    Map<String, Object> prompt(ChatCompletion chatCompletion);

}
