package org.project.soar.model.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.comment.dto.CommentResponse;
import org.project.soar.model.comment.service.CommentService;
import org.project.soar.model.user.User;
import org.project.soar.model.user.dto.UserInfoResponse;
import org.project.soar.model.user.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @GetMapping("/")
    @Operation(summary="전체 댓글 조회", description="모든 댓글을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAllComment() {
        List<CommentResponse> result = commentService.getAllComment();
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/policy/{policyId}")
    @Operation(summary="특정 정책의 모든 댓글 조회", description="특정 정책의 모든 댓글을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAllCommentByPolicyID(@PathVariable(value = "policyId") String policyId) {
        List<CommentResponse> result = commentService.getAllCommentByPolicyId(policyId);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary="특정 사용자의 모든 댓글 조회", description="특정 사용자의 모든 댓글을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAllCommentByUserId(@PathVariable(value = "userId") String userId) {
        List<CommentResponse> result = commentService.getAllCommentByUserId(userId);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @PostMapping("/")
    @Operation(summary="새로운 댓글 생성", description="새로운 댓글을 생성합니다.")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> createComment(@RequestBody CommentRequest commentRequest) {
        List<CommentResponse> result = commentService.createComment(commentRequest);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "댓글 수정", description = "ID에 해당하는 댓글을 수정합니다.")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id,
            @RequestBody CommentRequest commentRequest) {
        CommentResponse result = commentService.updateComment(id, commentRequest);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "댓글 삭제", description = "ID에 해당하는 댓글을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

    @GetMapping("/applied/count")
    public ResponseEntity<ApiResponse<?>> getAppliedPolicyCount(HttpServletRequest request) {
        User user = getUserFromToken(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }

        int count = commentService.getCommentCount(user);
        return ResponseEntity.ok(ApiResponse.createSuccessWithMessage(count, "작성한 댓글 개수 조회됨"));
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private User getUserFromToken(HttpServletRequest request) {
        String token = extractAccessToken(request);
        if (token == null)
            return null;

        try {
            String subject = tokenProvider.validateTokenAndGetSubject(token);
            String[] parts = subject.split(":");
            if (parts.length < 1)
                return null;
            Long userId = Long.parseLong(parts[0]);
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

}