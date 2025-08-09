package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.UserYouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.dto.YouthPolicyApplyResponseDto;
import org.project.soar.model.youthpolicy.repository.UserYouthPolicyRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserYouthPolicyService {

    private final YouthPolicyRepository youthPolicyRepository;
    private final UserYouthPolicyRepository userYouthPolicyRepository;
    private final UserRepository userRepository;

    /**
     * 정책 신청 처리 로직
     *
     * @param user     로그인된 사용자
     * @param policyId 신청할 정책 ID
     * @return 신청 URL (이미 신청 or 정책 종료 시 null)
     * @throws IllegalArgumentException 정책이 존재하지 않을 경우
     */
     public YouthPolicyApplyResponseDto applyToPolicy(User user, String policyId) {
        YouthPolicy policy = youthPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 정책을 찾을 수 없습니다."));

        EndStatus endStatus = getEndStatus(policy);

        if (endStatus == EndStatus.BUSINESS_ENDED) {
            return new YouthPolicyApplyResponseDto(
                    null,
                    "본 사업은 종료되어 신청할 수 없습니다.",
                    policyId,
                    user.getUserId()
            );
        }
        if (endStatus == EndStatus.APPLY_ENDED) {
            return new YouthPolicyApplyResponseDto(
                    null,
                    "해당 정책의 신청이 마감되어 신청할 수 없습니다.",
                    policyId,
                    user.getUserId()
            );
        }

        // 이미 신청했는지 확인
        boolean alreadyApplied = userYouthPolicyRepository.findByUserAndPolicy(user, policy).isPresent();
        if (alreadyApplied) {
            return new YouthPolicyApplyResponseDto(
                    null,
                    "이미 신청한 정책입니다.",
                    policyId,
                    user.getUserId()
            );
        }

        // 신청 저장
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

    /** 종료 상태 판정 */
    private EndStatus getEndStatus(YouthPolicy p) {
        LocalDate today = LocalDate.now();

        // 1) dateType 최우선
        if ("FINISHED".equalsIgnoreCase(p.getDateType())) {
            // label 로 구분 시도
            String label = p.getDateLabel();
            if (label != null && label.contains("신청 마감")) return EndStatus.APPLY_ENDED;
            return EndStatus.BUSINESS_ENDED;
        }

        // 2) dateLabel 기반 (단, D-가 있으면 진행 중으로 간주)
        String label = p.getDateLabel();
        if (label != null) {
            boolean hasDday = label.contains("D-");
            if (!hasDday) {
                if (label.contains("신청 마감")) return EndStatus.APPLY_ENDED;
                if (label.contains("사업 종료")) return EndStatus.BUSINESS_ENDED;
            }
        }

        // 3) 실제 날짜 기반 (신청 마감)
        if (p.getApplicationEndDate() != null &&
                p.getApplicationEndDate().toLocalDate().isBefore(today)) {
            return EndStatus.APPLY_ENDED;
        }

        // 4) 실제 날짜 기반 (사업 종료)
        LocalDate bizEnd = parseYyyyMMdd(p.getBusinessPeriodEnd());
        if (bizEnd != null && bizEnd.isBefore(today)) {
            return EndStatus.BUSINESS_ENDED;
        }

        return EndStatus.NONE;
    }

    private LocalDate parseYyyyMMdd(String yyyymmdd) {
        try {
            if (yyyymmdd == null || yyyymmdd.isBlank()) return null;
            return LocalDate.parse(yyyymmdd.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }

    private enum EndStatus { NONE, APPLY_ENDED, BUSINESS_ENDED }

    /**
     * 실시간 인기 지원사업
     */
    public List<YouthPolicy> getPopularPolicies() {
        try {
            return userYouthPolicyRepository.findTop10ByApplicationCount((PageRequest.of(0, 10)));
        } catch (Exception e) {
            throw new RuntimeException("실시간 인기 지원사업 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 나이대별 실시간 인기 지원사업
     */
    public List<YouthPolicy> getPopularPoliciesAge(Long userId) {
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            // 실제 나이 계산
            int age = LocalDate.now().getYear() - user.getUserBirthDate().getYear();
            if (LocalDate.now().getDayOfYear() < user.getUserBirthDate().getDayOfYear()) {
                age--;
            }
            // 10대/20대/30대... 나이대 계산
            int ageGroup = (age / 10) * 10;

            // 나이대에 맞는 정책 Top 10 조회
            return userYouthPolicyRepository.findTop10PopularByAgeGroup(ageGroup, PageRequest.of(0, 10));
        } catch (Exception e) {
            throw new RuntimeException("실시간 인기 지원사업 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 신청한 정책 개수 조회
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
