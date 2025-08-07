package org.project.soar.model.comment.repository;

import org.project.soar.model.comment.Comment;
import org.project.soar.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // User 엔티티 내부의 userId 값을 기준으로 검색
    List<Comment> findByUser_UserId(Long userId);

    // YouthPolicy 엔티티 내부의 policyId 값을 기준으로 검색
    List<Comment> findByYouthPolicy_PolicyId(String policyId);

    void deleteAllByUser(User user);

    Integer countByUser(User user);
}
