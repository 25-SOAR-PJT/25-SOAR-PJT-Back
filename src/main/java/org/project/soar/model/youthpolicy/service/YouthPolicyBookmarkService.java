package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyBookmark;
import org.project.soar.model.youthpolicy.dto.YouthPolicyBookmarkResponseDto;
import org.project.soar.model.youthpolicy.dto.YouthPolicyLatestResponseDto;
import org.project.soar.model.youthpolicy.repository.YouthPolicyBookmarkRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouthPolicyBookmarkService {

    private final YouthPolicyBookmarkRepository bookmarkRepository;
    private final YouthPolicyRepository youthPolicyRepository;
    private final UserRepository userRepository;

    /**
     * 북마크 토글 (있으면 해제, 없으면 추가)
     * - 정상 처리: true(추가됨) / false(해제됨)
     * - 정책 없음: null (컨트롤러에서 400 등으로 매핑)
     */
    public Boolean toggleBookmark(User user, String policyId) {
        YouthPolicy policy = youthPolicyRepository.findById(policyId).orElse(null);
        if (policy == null) {
            return null; // 정책 없음 → 컨트롤러에서 에러 응답으로 변환
        }

        return bookmarkRepository.findByUserAndPolicy(user, policy)
                .map(existing -> {
                    bookmarkRepository.delete(existing); // 해제
                    return Boolean.FALSE; // 북마크 취소됨
                })
                .orElseGet(() -> {
                    YouthPolicyBookmark newBookmark = YouthPolicyBookmark.builder()
                            .user(user)
                            .policy(policy)
                            .build();
                    bookmarkRepository.save(newBookmark); // 추가
                    return Boolean.TRUE; // 북마크 추가됨
                });
    }

    /**
     * 특정 정책 북마크 해제 (idempotent)
     * - 정책 코드가 잘못되어도 조용히 종료(no-op)
     */
    public void unbookmark(User user, String policyId) {
        YouthPolicy policy = youthPolicyRepository.findById(policyId).orElse(null);
        if (policy == null) {
            return; // 정책 없음 → 아무 것도 하지 않음
        }
        bookmarkRepository.findByUserAndPolicy(user, policy)
                .ifPresent(bookmarkRepository::delete);
    }

    /**
     * 사용자 모든 정책 북마크 해제 (전체 삭제, idempotent)
     */
    public void unbookmarkAll(User user) {
        List<YouthPolicyBookmark> all = bookmarkRepository.findAllByUser(user);
        if (!all.isEmpty()) {
            bookmarkRepository.deleteAll(all);
        }
    }

    /**
     * 사용자 북마크 전체 조회 (DTO)
     */
    public List<YouthPolicyBookmarkResponseDto> getUserBookmarkDtos(User user) {
        List<YouthPolicyBookmark> bookmarks = bookmarkRepository.findAllByUser(user);
        return bookmarks.stream()
                .map(bookmark -> YouthPolicyBookmarkResponseDto.from(bookmark.getPolicy()))
                .collect(Collectors.toList());
    }

    /**
     * 북마크 여부
     * - true/false: 정책 존재
     * - null: 정책 없음 (컨트롤러에서 에러 응답으로 변환)
     */
    public Boolean isBookmarked(User user, String policyId) {
        YouthPolicy policy = youthPolicyRepository.findById(policyId).orElse(null);
        if (policy == null) {
            return null; // 정책 없음
        }
        return bookmarkRepository.findByUserAndPolicy(user, policy).isPresent();
    }

    /**
     * 실시간 인기 지원사업 (북마크 기준 Top 5)
     */
    public List<YouthPolicy> getPopularPolicies() {
        try {
            return bookmarkRepository.findPopularByBookmarks(PageRequest.of(0, 5));
        } catch (Exception e) {
            throw new RuntimeException("실시간 인기 지원사업 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 나이대별 실시간 인기 지원사업 (Top 5)
     */
    public List<YouthPolicy> getPopularPoliciesAge(Long userId) {
        try {
            var user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            int age = LocalDate.now().getYear() - user.getUserBirthDate().getYear();
            if (LocalDate.now().getDayOfYear() < user.getUserBirthDate().getDayOfYear()) {
                age--;
            }
            int ageGroup = (age / 10) * 10;
            return bookmarkRepository.findPopularBookmarksByAgeGroup(ageGroup, PageRequest.of(0, 5));
        } catch (Exception e) {
            throw new RuntimeException("실시간 인기 지원사업 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 북마크 중 종료일이 가장 가까운 정책 1건
     */
    public YouthPolicyLatestResponseDto getLatestBookmarkByEndDate(Long userId) {
        var user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<YouthPolicy> result = bookmarkRepository.findLatestBookmarkByEndDate(user);
        if (result.isEmpty()) {
            return null; // 북마크가 없을 경우 null
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

                if (daysBetween > 0)
                    dDayStr = "D-" + daysBetween;
                else if (daysBetween == 0)
                    dDayStr = "D-Day";
                else
                    dDayStr = "마감";
            } catch (Exception ignored) {
                dDayStr = null; // 형식 불일치 시 표시 안 함
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
