package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.UserYouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.dto.ApplyStatus;
import org.project.soar.model.youthpolicy.dto.YouthPolicyApplyResponseDto;
import org.project.soar.model.youthpolicy.dto.YouthPolicyBulkApplyItemResultDto;
import org.project.soar.model.youthpolicy.dto.YouthPolicyBulkApplyResponseDto;
import org.project.soar.model.youthpolicy.repository.UserYouthPolicyRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.project.soar.util.DateClassifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserYouthPolicyService {

    private final YouthPolicyRepository youthPolicyRepository;
    private final UserYouthPolicyRepository userYouthPolicyRepository;
    private final UserRepository userRepository;

    /**
     * 단건 신청
     */
    public YouthPolicyApplyResponseDto applyToPolicy(User user, String policyId) {
        YouthPolicy policy = youthPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 정책을 찾을 수 없습니다."));

        ApplyStatus status = resolveApplyStatus(policy);
        if (status == ApplyStatus.OPEN_UPCOMING) {
            return new YouthPolicyApplyResponseDto(
                    null, "아직 오픈되지 않았습니다. 오픈 예정 상태입니다.", policyId, user.getUserId());
        }
        if (status == ApplyStatus.BUSINESS_ENDED) {
            return new YouthPolicyApplyResponseDto(
                    null, "본 사업은 종료되어 신청할 수 없습니다.", policyId, user.getUserId());
        }
        if (status == ApplyStatus.APPLY_ENDED) {
            return new YouthPolicyApplyResponseDto(
                    null, "해당 정책의 신청이 마감되어 신청할 수 없습니다.", policyId, user.getUserId());
        }

        boolean alreadyApplied = userYouthPolicyRepository.findByUserAndPolicy(user, policy).isPresent();
        if (alreadyApplied) {
            return new YouthPolicyApplyResponseDto(
                    null, "이미 신청한 정책입니다.", policyId, user.getUserId());
        }

        UserYouthPolicy userPolicy = UserYouthPolicy.builder()
                .user(user)
                .policy(policy)
                .appliedAt(LocalDateTime.now())
                .build();
        userYouthPolicyRepository.save(userPolicy);

        return new YouthPolicyApplyResponseDto(
                policy.getApplyUrl(),
                (policy.getApplyUrl() == null || policy.getApplyUrl().isBlank())
                        ? "정책 신청이 완료되었습니다. (이동할 신청 URL은 없습니다)"
                        : "정책 신청이 완료되었습니다.",
                policyId,
                user.getUserId());
    }

    /**
     * 여러 정책 동시 신청
     * - URL은 반환하지 않으며, 항목별 결과와 카운트만 반환한다.
     * - 존재하지 않는 정책, 오픈예정, 마감, 종료, 이미 신청을 각각 구분하여 결과에 담는다.
     */
    public YouthPolicyBulkApplyResponseDto applyToPolicies(User user, List<String> policyIds) {
        if (policyIds == null || policyIds.isEmpty()) {
            return YouthPolicyBulkApplyResponseDto.builder()
                    .userId(user.getUserId())
                    .requestedCount(0)
                    .appliedCount(0)
                    .alreadyAppliedCount(0)
                    .applyEndedCount(0)
                    .businessEndedCount(0)
                    .openUpcomingCount(0)
                    .notFoundCount(0)
                    .results(List.of())
                    .build();
        }

        // 중복 제거(요청 순서 유지)
        LinkedHashSet<String> dedup = new LinkedHashSet<>(policyIds);
        List<String> distinctIds = new ArrayList<>(dedup);

        // 정책 일괄 조회
        List<YouthPolicy> found = youthPolicyRepository.findAllById(distinctIds);
        Map<String, YouthPolicy> policyMap = new HashMap<>();
        for (YouthPolicy p : found)
            policyMap.put(p.getPolicyId(), p);

        // 이미 신청한 항목을 한 번에 조회
        Set<String> alreadyAppliedIds = userYouthPolicyRepository.findAppliedPolicyIds(user, found);

        int applied = 0, already = 0, applyEnded = 0, businessEnded = 0, openUpcoming = 0, notFound = 0;
        List<YouthPolicyBulkApplyItemResultDto> results = new ArrayList<>();
        List<UserYouthPolicy> toSave = new ArrayList<>();

        for (String pid : distinctIds) {
            YouthPolicy policy = policyMap.get(pid);
            if (policy == null) {
                results.add(item(pid, ApplyStatus.NOT_FOUND, "정책을 찾을 수 없습니다.", user.getUserId()));
                notFound++;
                continue;
            }

            ApplyStatus status = resolveApplyStatus(policy);
            if (status == ApplyStatus.OPEN_UPCOMING) {
                results.add(item(pid, status, "아직 오픈되지 않았습니다. 오픈 예정 상태입니다.", user.getUserId()));
                openUpcoming++;
                continue;
            }
            if (status == ApplyStatus.BUSINESS_ENDED) {
                results.add(item(pid, status, "본 사업은 종료되어 신청할 수 없습니다.", user.getUserId()));
                businessEnded++;
                continue;
            }
            if (status == ApplyStatus.APPLY_ENDED) {
                results.add(item(pid, status, "해당 정책의 신청이 마감되어 신청할 수 없습니다.", user.getUserId()));
                applyEnded++;
                continue;
            }

            if (alreadyAppliedIds.contains(pid)) {
                results.add(item(pid, ApplyStatus.ALREADY_APPLIED, "이미 신청한 정책입니다.", user.getUserId()));
                already++;
                continue;
            }

            toSave.add(UserYouthPolicy.builder()
                    .user(user)
                    .policy(policy)
                    .appliedAt(LocalDateTime.now())
                    .build());
            results.add(item(pid, ApplyStatus.APPLIED, "정책 신청이 완료되었습니다.", user.getUserId()));
            applied++;
        }

        if (!toSave.isEmpty()) {
            userYouthPolicyRepository.saveAll(toSave);
        }

        return YouthPolicyBulkApplyResponseDto.builder()
                .userId(user.getUserId())
                .requestedCount(distinctIds.size())
                .appliedCount(applied)
                .alreadyAppliedCount(already)
                .applyEndedCount(applyEnded)
                .businessEndedCount(businessEnded)
                .openUpcomingCount(openUpcoming)
                .notFoundCount(notFound)
                .results(results)
                .build();
    }

    private YouthPolicyBulkApplyItemResultDto item(String pid, ApplyStatus s, String msg, Long userId) {
        return YouthPolicyBulkApplyItemResultDto.builder()
                .policyId(pid)
                .status(s)
                .message(msg)
                .userId(userId)
                .build();
    }

    /**
     * DateClassifier 기반의 상태 판정
     * - applicationStartDate, applicationEndDate는 LocalDateTime이므로 LocalDate로 변환하여
     * 사용한다.
     * - 시작일이 오늘보다 미래면 UPCOMING
     * - 종료/마감 텍스트 보조 판단은 DateClassifier 내부에 있다.
     */
    private ApplyStatus resolveApplyStatus(YouthPolicy p) {
        try {
            LocalDate applyStart = (p.getApplicationStartDate() != null)
                    ? p.getApplicationStartDate().toLocalDate()
                    : null;
            LocalDate applyEnd = (p.getApplicationEndDate() != null)
                    ? p.getApplicationEndDate().toLocalDate()
                    : null;

            DateClassifier.DateResult r = DateClassifier.classify(
                    applyStart,
                    applyEnd,
                    p.getBusinessPeriodEnd(),
                    p.getApplyMethodContent(),
                    p.getScreeningMethodContent(),
                    p.getBusinessPeriodEtc(),
                    p.getPolicySupportContent(),
                    p.getPolicyName(),
                    LocalDate.now());
            return ApplyStatus.fromDateResult(r);
        } catch (Exception ignored) {
            EndStatus end = getEndStatus(p);
            return switch (end) {
                case APPLY_ENDED -> ApplyStatus.APPLY_ENDED;
                case BUSINESS_ENDED -> ApplyStatus.BUSINESS_ENDED;
                default -> ApplyStatus.ELIGIBLE;
            };
        }
    }

    /**
     * 기존 종료 상태 판정 (폴백용)
     */
    private EndStatus getEndStatus(YouthPolicy p) {
        LocalDate today = LocalDate.now();

        if ("FINISHED".equalsIgnoreCase(p.getDateType())) {
            String label = p.getDateLabel();
            if (label != null && label.contains("신청 마감"))
                return EndStatus.APPLY_ENDED;
            return EndStatus.BUSINESS_ENDED;
        }

        String label = p.getDateLabel();
        if (label != null) {
            boolean hasDday = label.contains("D-");
            if (!hasDday) {
                if (label.contains("신청 마감"))
                    return EndStatus.APPLY_ENDED;
                if (label.contains("사업 종료"))
                    return EndStatus.BUSINESS_ENDED;
            }
        }

        if (p.getApplicationEndDate() != null &&
                p.getApplicationEndDate().toLocalDate().isBefore(today)) {
            return EndStatus.APPLY_ENDED;
        }

        LocalDate bizEnd = parseYyyyMMdd(p.getBusinessPeriodEnd());
        if (bizEnd != null && bizEnd.isBefore(today)) {
            return EndStatus.BUSINESS_ENDED;
        }

        return EndStatus.NONE;
    }

    private LocalDate parseYyyyMMdd(String yyyymmdd) {
        try {
            if (yyyymmdd == null || yyyymmdd.isBlank())
                return null;
            return LocalDate.parse(yyyymmdd.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }

    private enum EndStatus {
        NONE, APPLY_ENDED, BUSINESS_ENDED
    }

    public int getAppliedPolicyCount(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        int count = userYouthPolicyRepository.countByUser(user);
        if (count < 0) {
            throw new RuntimeException("신청한 정책 개수 조회 중 오류가 발생했습니다.");
        }
        return count;
    }
}
