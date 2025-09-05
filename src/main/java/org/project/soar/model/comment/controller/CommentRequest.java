package org.project.soar.model.comment.controller;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class CommentRequest {
    private String comment;
    private String policyId;
}
