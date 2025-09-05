package org.project.soar.model.comment.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long commentId;
    private String comment;
    private String policyId;
    private Long userId;
    private String userName;
    private LocalDateTime createdDate;
    @Builder
    public CommentResponse(Long commentId, String comment, String policyId, Long userId, String userName, LocalDateTime createdDate) {
        this.commentId = commentId;
        this.comment = comment;
        this.policyId = policyId;
        this.userId = userId;
        this.userName = userName;
        this.createdDate = createdDate;
    }
}
