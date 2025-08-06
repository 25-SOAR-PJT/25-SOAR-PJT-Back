package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.user.User;
import org.project.soar.model.user.dto.SignInResponse;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyBookmark;
import org.project.soar.model.youthpolicy.dto.YouthPolicyBookmarkResponseDto;
import org.project.soar.model.youthpolicy.dto.YouthPolicyLatestResponseDto;
import org.project.soar.model.youthpolicy.repository.YouthPolicyBookmarkRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouthPolicyBookmarkService {

    private final YouthPolicyBookmarkRepository bookmarkRepository;
    private final YouthPolicyRepository youthPolicyRepository;
    private final UserRepository userRepository;

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

    public YouthPolicyLatestResponseDto getLatestBookmarkByEndDate(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<YouthPolicy> result = bookmarkRepository.findLatestBookmarkByEndDate(user);
        if (result.isEmpty()) {
            return null; // 북마크가 없을 경우 null 반환
        }
        YouthPolicy latestPolicy = result.get(0);

        String dDayStr = null;
        String endDateStr = latestPolicy.getBusinessPeriodEnd();
        if (endDateStr != null && !endDateStr.isBlank()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate today = LocalDate.now();
                LocalDate endDate = LocalDate.parse(endDateStr, formatter);

                long daysBetween = ChronoUnit.DAYS.between(today, endDate);

                if (daysBetween > 0) {
                    dDayStr = "D-" + daysBetween;
                } else if (daysBetween == 0) {
                    dDayStr = "D-Day";
                } else {
                    dDayStr = "마감";
                }
            } catch (Exception e) {
                dDayStr = null; // 형식이 안 맞으면 D-Day 표시 안 함
            }
        }
        return YouthPolicyLatestResponseDto.builder()
                .policyId(latestPolicy.getPolicyId())
                .policyName(latestPolicy.getPolicyName())
                .businessPeriodEnd(latestPolicy.getBusinessPeriodEnd())
                .dDay(dDayStr)
                .build();
    }

}
