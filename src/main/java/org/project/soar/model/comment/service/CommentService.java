package org.project.soar.model.comment.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.comment.Comment;
import org.project.soar.model.comment.controller.CommentRequest;
import org.project.soar.model.comment.dto.CommentResponse;
import org.project.soar.model.comment.repository.CommentRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final YouthPolicyRepository youthPolicyRepository;

    public List<CommentResponse> getAllComment() {
        List<Comment> comments = commentRepository.findAll();
        return comments.stream().map(
                comment -> new CommentResponse(
                        comment.getCommentId(),
                        comment.getComment(),
                        comment.getYouthPolicy().getPolicyId(),
                        comment.getUser().getUserId(),
                        comment.getUser().getUserName(),
                        comment.getCreateDate())
        ).collect(Collectors.toList());
    }

    public List<CommentResponse> getAllCommentByUserId(String userId) {
        List<Comment> comments = commentRepository.findByUser_UserId(Long.valueOf(userId));
        return comments.stream().map(
                comment -> new CommentResponse(
                        comment.getCommentId(),
                        comment.getComment(),
                        comment.getYouthPolicy().getPolicyId(),
                        comment.getUser().getUserId(),
                        comment.getUser().getUserName(),
                        comment.getCreateDate())
        ).collect(Collectors.toList());
    }

    public List<CommentResponse> getAllCommentByPolicyId(String policyId) {
        List<Comment> comments = commentRepository.findByYouthPolicy_PolicyId(policyId);
        return comments.stream().map(
                comment -> new CommentResponse(
                        comment.getCommentId(),
                        comment.getComment(),
                        comment.getYouthPolicy().getPolicyId(),
                        comment.getUser().getUserId(),
                        comment.getUser().getUserName(),
                        comment.getCreateDate())
        ).collect(Collectors.toList());
    }

    public List<CommentResponse> createComment(CommentRequest request) {
        User user = userRepository.findById(Long.valueOf(request.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        YouthPolicy policy = youthPolicyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 정책입니다."));

        Comment comment = Comment.builder()
                .comment(request.getComment())
                .user(user)
                .youthPolicy(policy)
                .build();

        commentRepository.save(comment);

        return getAllCommentByPolicyId(request.getPolicyId());
    }

    public CommentResponse updateComment(Long id, CommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        comment.setComment(request.getComment());
        Comment updated = commentRepository.save(comment);
        return new CommentResponse(
                updated.getCommentId(),
                updated.getComment(),
                updated.getYouthPolicy().getPolicyId(),
                updated.getUser().getUserId(),
                updated.getUser().getUserName(),
                updated.getCreateDate()
        );
    }

    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
        }
        commentRepository.deleteById(id);
    }

    public int getCommentCount(User user) {
        return commentRepository.countByUser(user);
    }

}
