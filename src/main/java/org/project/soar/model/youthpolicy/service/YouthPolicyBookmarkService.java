package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyBookmark;
import org.project.soar.model.youthpolicy.dto.YouthPolicyBookmarkResponseDto;
import org.project.soar.model.youthpolicy.repository.YouthPolicyBookmarkRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouthPolicyBookmarkService {

    private final YouthPolicyBookmarkRepository bookmarkRepository;
    private final YouthPolicyRepository youthPolicyRepository;

    public boolean toggleBookmark(User user, String policyId) {
        YouthPolicy policy = youthPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다."));

        return bookmarkRepository.findByUserAndPolicy(user, policy)
                .map(existing -> {
                    bookmarkRepository.delete(existing);
                    return false; // 북마크 취소
                })
                .orElseGet(() -> {
                    YouthPolicyBookmark newBookmark = YouthPolicyBookmark.builder()
                            .user(user)
                            .policy(policy)
                            .build();
                    bookmarkRepository.save(newBookmark);
                    return true; // 북마크 추가
                });
    }

    // 사용자 북마크 조회
    public List<YouthPolicyBookmarkResponseDto> getUserBookmarkDtos(User user) {
        List<YouthPolicyBookmark> bookmarks = bookmarkRepository.findAllByUser(user);

        return bookmarks.stream()
                .map(bookmark -> YouthPolicyBookmarkResponseDto.from(bookmark.getPolicy()))
                .collect(Collectors.toList());
    }


    public boolean isBookmarked(User user, String policyId) {
        YouthPolicy policy = youthPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다."));
        return bookmarkRepository.findByUserAndPolicy(user, policy).isPresent();
    }
}
