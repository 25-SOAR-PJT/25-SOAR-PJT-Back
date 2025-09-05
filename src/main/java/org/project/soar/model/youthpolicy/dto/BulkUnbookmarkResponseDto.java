package org.project.soar.model.youthpolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BulkUnbookmarkResponseDto {
    private int requestedCount;          // 요청한 정책 수(중복 제거 전 or 후는 아래 구현 설명 참고)
    private int removedCount;            // 실제 삭제된 북마크 수
    private int skippedCount;            // 이미 해제되었거나 존재하지 않아 스킵된 수
    private List<String> removedPolicyIds; // 실제로 해제된 정책 ID 목록
}