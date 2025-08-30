package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicyBookmark;
import org.project.soar.model.youthpolicy.dto.*;
import org.project.soar.model.youthpolicy.repository.UserYouthPolicyRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyBookmarkRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.project.soar.model.youthpolicytag.repository.TagByPolicyProjection;
import org.project.soar.model.youthpolicytag.repository.YouthPolicyTagRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouthPolicyBookmarkService {

    private final YouthPolicyBookmarkRepository bookmarkRepository;
    private final YouthPolicyRepository youthPolicyRepository;

    // ▼ 추가 주입
    private final UserYouthPolicyRepository userYouthPolicyRepository;
    private final YouthPolicyTagRepository youthPolicyTagRepository;

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
    public List<YouthPolicy> getPopularPoliciesAge(User user) {
        try {
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
    public YouthPolicyLatestResponseDto getLatestBookmarkByEndDate(User user) {
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

    /**
     * 북마크 + 신청여부 + 태그리스트 일괄 반환
     */
    public List<YouthPolicyBookmarkWithMetaResponseDto> getUserBookmarksWithMeta(User user) {
        // 1) 북마크 전부 조회(정책 포함)
        List<YouthPolicyBookmark> bookmarks = bookmarkRepository.findAllByUser(user);
        if (bookmarks.isEmpty()) return List.of();

        Collections.reverse(bookmarks);

        // 2) 정책 목록/ID 추출 (표시 순서 유지)
        List<YouthPolicy> policies = bookmarks.stream()
                .map(YouthPolicyBookmark::getPolicy)
                .filter(Objects::nonNull)
                .toList();
        List<String> policyIds = policies.stream().map(YouthPolicy::getPolicyId).toList();

        // 3) 사용자 신청 완료된 정책 ID 일괄 조회
        Set<String> appliedIds = userYouthPolicyRepository.findAppliedPolicyIds(user, policies);

        // 4) 정책별 태그 일괄 조회 → policyId -> List<TagSimpleDto>
        List<TagByPolicyProjection> tagRows = youthPolicyTagRepository.findTagsByPolicyIds(policyIds);
        Map<String, List<TagSimpleDto>> tagMap = tagRows.stream()
                .collect(Collectors.groupingBy(
                        TagByPolicyProjection::getPolicyId,
                        Collectors.mapping(r -> TagSimpleDto.builder()
                                .tagId(r.getTagId())
                                .tagName(r.getTagName())
                                .fieldId(r.getFieldId())
                                .build(), Collectors.toList())
                ));

        // 5) 최종 DTO 매핑(북마크 순서 보존)
        List<YouthPolicyBookmarkWithMetaResponseDto> result = new ArrayList<>(policies.size());
        for (YouthPolicy p : policies) {
            boolean applied = appliedIds.contains(p.getPolicyId());
            List<TagSimpleDto> tags = tagMap.getOrDefault(p.getPolicyId(), List.of());
            result.add(YouthPolicyBookmarkWithMetaResponseDto.from(p, applied, tags));
        }
        return result;
    }

    /**
     * 북마크 일괄 해제
     * - 존재하지 않거나 이미 해제된 항목은 건너뜀
     * - 입력 중복은 내부에서 제거
     */
    public BulkUnbookmarkResponseDto unbookmarkMany(User user, List<String> policyIds) {
        if (policyIds == null || policyIds.isEmpty()) {
            return BulkUnbookmarkResponseDto.builder()
                    .requestedCount(0)
                    .removedCount(0)
                    .skippedCount(0)
                    .removedPolicyIds(List.of())
                    .build();
        }

        // 공백/Null 제거 + 중복 제거
        List<String> distinctIds = policyIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            return BulkUnbookmarkResponseDto.builder()
                    .requestedCount(0)
                    .removedCount(0)
                    .skippedCount(0)
                    .removedPolicyIds(List.of())
                    .build();
        }

        // 실제로 현재 사용자에게 존재하는 북마크만 조회
        List<YouthPolicyBookmark> toDelete =
                bookmarkRepository.findAllByUserAndPolicyPolicyIdIn(user, distinctIds);

        List<String> removedIds = toDelete.stream()
                .map(b -> b.getPolicy().getPolicyId())
                .toList();

        // 일괄 삭제
        if (!toDelete.isEmpty()) {
            bookmarkRepository.deleteAllInBatch(toDelete);
        }

        int removed = removedIds.size();
        int requested = distinctIds.size();
        int skipped = requested - removed;  // 이미 해제/존재X 등

        return BulkUnbookmarkResponseDto.builder()
                .requestedCount(requested)
                .removedCount(removed)
                .skippedCount(skipped)
                .removedPolicyIds(removedIds)
                .build();
    }

    /**
     * 실시간 인기 지원사업 (북마크 기준 Top 5)
     */
    public List<YouthPolicyPopularView> getPopularPoliciesName() {
        try {
            return bookmarkRepository.findPopularByBookmarksName(PageRequest.of(0, 5));
        } catch (Exception e) {
            throw new RuntimeException("실시간 인기 지원사업 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 나이대별 실시간 인기 지원사업 (Top 5)
     * - 유저의 생년월일로 나이를 계산하고, 10년 단위 ageGroup(예: 20, 30)으로 버킷팅
     * - 해당 버킷에서 북마크가 많은 정책을 Top 5 조회
     * - 현재 유저의 북마크 여부와 ageGroup 라벨(“20대”)을 DTO에 포함해서 반환
     */
    public List<YouthPolicyUserAgeItemDto> getPopularPoliciesUserAge(User user) {
        if (user == null) {
            return List.of();
        }
        if (user.getUserBirthDate() == null) {
            // 생년월일 없으면 비어있는 리스트 반환 (컨트롤러에서 400 처리해도 됨)
            return List.of();
        }

        // 정확한 만 나이 계산
        int age = Period.between(user.getUserBirthDate(), LocalDate.now()).getYears();
        int ageGroup = (age / 10) * 10;                 // 23 -> 20, 31 -> 30
        String ageGroupLabel = ageGroup + "대";          // "20대" 형식

        // 나이대별 인기 Top 5 (native + Pageable LIMIT)
        var pageable = PageRequest.of(0, 5);
        List<YouthPolicy> policies =
                bookmarkRepository.findPopularBookmarksByAgeGroup(ageGroup, pageable);

        if (policies.isEmpty()) {
            return List.of();
        }

        // 현재 유저가 북마크한 항목 체크
        List<String> ids = policies.stream().map(YouthPolicy::getPolicyId).toList();
        List<String> bookmarkedIds = bookmarkRepository.findBookmarkedPolicyIds(user, ids);
        var bookmarkedSet = new HashSet<>(bookmarkedIds);

        // DTO 매핑
        return policies.stream()
                .map(p -> YouthPolicyUserAgeItemDto.builder()
                        .policyId(p.getPolicyId())
                        .policyName(p.getPolicyName())
                        .supervisingInstName(p.getSupervisingInstName())
                        .dateLabel(p.getDateLabel())              // 이미 엔티티에 라벨이 있으면 그대로 사용
                        .bookmarked(bookmarkedSet.contains(p.getPolicyId()))
                        .ageGroup(ageGroupLabel)
                        .build())
                .toList();
    }
}
