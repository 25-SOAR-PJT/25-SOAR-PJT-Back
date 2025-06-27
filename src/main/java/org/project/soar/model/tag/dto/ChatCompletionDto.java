package org.project.soar.model.tag.dto;

import lombok.*;

import java.util.List;


@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatCompletionDto {

    // 사용할 모델
    private String model;

    private List<ChatRequestMsgDto> messages;

    // 프롬프트의 다양성을 조절할 명령어(default : 1)
    private float temperature = 1;

    // 최대 사용할 토큰(default : 16)
    private int max_tokens = 16;

    @Builder
    public ChatCompletionDto(String model, List<ChatRequestMsgDto> messages) {
        this.model = model;
        this.messages = messages;
    }
}