package org.project.soar.model.tag.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptRequest {
    private Prompt prompt;
    private List<Object> input;
    private Map<String, Object> reasoning;
    private Integer max_output_tokens;
    private Boolean store;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Prompt {
        private String id;
        private String version;
    }
}

