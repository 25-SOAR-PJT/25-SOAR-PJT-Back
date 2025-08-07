package org.project.soar.model.youthpolicy.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.project.soar.model.youthpolicy.UserYouthPolicy;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.repository.UserYouthPolicyRepository;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public String applyToPolicy(User user, String policyId) {
        YouthPolicy policy = youthPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 정책을 찾을 수 없습니다."));

        // 종료된 정책인지 확인 (예: dateLabel에 "종료" 포함된 경우)
        if (policy.getDateLabel() != null && policy.getDateLabel().contains("종료")) {
            return null;
        }

        // 이미 신청했는지 확인
        boolean alreadyApplied = userYouthPolicyRepository.findByUserAndPolicy(user, policy).isPresent();
        if (alreadyApplied) {
            return null;
        }

        // 신청 정보 저장
        UserYouthPolicy userPolicy = UserYouthPolicy.builder()
                .user(user)
                .policy(policy)
                .appliedAt(LocalDateTime.now())
                .build();

        userYouthPolicyRepository.save(userPolicy);

        return policy.getApplyUrl(); // applyUrl 필드명 맞춤 (getter 있음 확인)
    }

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
