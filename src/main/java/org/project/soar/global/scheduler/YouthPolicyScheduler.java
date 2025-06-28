package org.project.soar.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.soar.model.youthpolicy.service.YouthPolicyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class YouthPolicyScheduler {

    private final YouthPolicyService youthPolicyService;

    /**
     * 매일 새벽 2시에 청년정책 데이터 동기화
     * cron: 초(0-59) 분(0-59) 시(0-23) 일(1-31) 월(1-12) 요일(0-7, 0과 7은 일요일)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void syncYouthPolicyDataDaily() {
        log.info("=== 일일 청년정책 데이터 동기화 시작 - {} ===",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            int syncedCount = youthPolicyService.syncAllYouthPolicies();
            log.info("=== 일일 청년정책 데이터 동기화 완료 - 총 {}개 정책 동기화 ===", syncedCount);
        } catch (Exception e) {
            log.error("=== 일일 청년정책 데이터 동기화 실패 ===", e);
        }
    }

    /**
     * 매주 일요일 새벽 3시에 전체 데이터 재동기화 (데이터 정합성 확보)
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void fullSyncYouthPolicyDataWeekly() {
        log.info("=== 주간 청년정책 전체 데이터 재동기화 시작 - {} ===",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            // 기존 데이터 삭제 후 전체 재동기화
            youthPolicyService.deleteAllYouthPolicies();
            int syncedCount = youthPolicyService.syncAllYouthPolicies();
            log.info("=== 주간 청년정책 전체 데이터 재동기화 완료 - 총 {}개 정책 동기화 ===", syncedCount);
        } catch (Exception e) {
            log.error("=== 주간 청년정책 전체 데이터 재동기화 실패 ===", e);
        }
    }

    /**
     * 매 1시간마다 최신 정책 확인 (개발/테스트용 - 필요시 활성화)
     */
    // @Scheduled(fixedRate = 3600000) // 1시간 = 3,600,000ms
    public void syncLatestYouthPoliciesHourly() {
        log.info("=== 시간별 최신 청년정책 확인 시작 - {} ===",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            // 최근 7일간 등록된 정책만 동기화
            int syncedCount = youthPolicyService.syncRecentYouthPolicies(7);
            log.info("=== 시간별 최신 청년정책 확인 완료 - {}개 정책 확인 ===", syncedCount);
        } catch (Exception e) {
            log.error("=== 시간별 최신 청년정책 확인 실패 ===", e);
        }
    }

    /**
     * 수동 트리거용 메서드 (Controller에서 호출)
     */
    public void manualSync() {
        log.info("=== 수동 청년정책 데이터 동기화 시작 - {} ===",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            int syncedCount = youthPolicyService.syncAllYouthPolicies();
            log.info("=== 수동 청년정책 데이터 동기화 완료 - 총 {}개 정책 동기화 ===", syncedCount);
        } catch (Exception e) {
            log.error("=== 수동 청년정책 데이터 동기화 실패 ===", e);
            throw new RuntimeException("청년정책 데이터 동기화에 실패했습니다.", e);
        }
    }
}