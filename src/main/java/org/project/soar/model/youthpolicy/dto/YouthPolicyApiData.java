package org.project.soar.model.youthpolicy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouthPolicyApiData {
    // 기본 정보
    private String plcyNo; // 정책번호
    private String plcyNm; // 정책명
    private String plcyKywdNm; // 정책키워드
    private String plcyExplnCn; // 정책설명
    private String plcySprtCn; // 지원내용

    // 신청 관련
    private String aplyUrlAddr; // 신청URL
    private String aplyYmd; // 신청기간
    private String plcyAplyMthdCn; // 정책신청방법내용
    private String srngMthdCn; // 심사방법내용
    private String sbmsnDcmntCn; // 제출서류내용

    // 기관 정보
    private String sprvsnInstCdNm; // 주관기관명
    private String operInstCdNm; // 운영기관명
    private String rgtrInstCdNm; // 등록기관명

    // 대상 관련
    private String sprtTrgtMinAge; // 지원대상최소나이
    private String sprtTrgtMaxAge; // 지원대상최대나이
    private String sprtSclCnt; // 지원규모

    // 분류
    private String lclsfNm; // 대분류명
    private String mclsfNm; // 중분류명

    // 기간 관련
    private String bizPrdBgngYmd; // 사업기간시작일
    private String bizPrdEndYmd; // 사업기간종료일
    private String bizPrdEtcCn; // 사업기간기타내용
    private String frstRegDt; // 최초등록일
    private String lastMdfcnDt; // 최종수정일

    // 기타
    private String etcMttrCn; // 기타사항내용
    private String refUrlAddr1; // 참고URL1
    private String refUrlAddr2; // 참고URL2
    private String inqCnt; // 조회수

    // 추가 필드들 (실제 API 응답에 포함된 모든 필드)
    private String bscPlanCycl; // 기본계획주기
    private String bscPlanPlcyWayNo; // 기본계획정책방식번호
    private String bscPlanFcsAsmtNo; // 기본계획중점평가번호
    private String bscPlanAsmtNo; // 기본계획평가번호
    private String pvsnInstGroupCd; // 제공기관그룹코드
    private String plcyPvsnMthdCd; // 정책제공방법코드
    private String plcyAprvSttsCd; // 정책승인상태코드
    private String sprvsnInstCd; // 주관기관코드
    private String sprvsnInstPicNm; // 주관기관담당자명
    private String operInstCd; // 운영기관코드
    private String operInstPicNm; // 운영기관담당자명
    private String sprtSclLmtYn; // 지원규모제한여부
    private String aplyPrdSeCd; // 신청기간구분코드
    private String bizPrdSeCd; // 사업기간구분코드
    private String sprtArvlSeqYn; // 지원도착순서여부
    private String sprtTrgtAgeLmtYn; // 지원대상연령제한여부
    private String mrgSttsCd; // 결혼상태코드
    private String earnCndSeCd; // 소득조건구분코드
    private String earnMinAmt; // 소득최소금액
    private String earnMaxAmt; // 소득최대금액
    private String earnEtcCn; // 소득기타내용
    private String addAplyQlfcCndCn; // 추가신청자격조건내용
    private String ptcpPrpTrgtCn; // 참여목적대상내용
    private String rgtrInstCd; // 등록기관코드
    private String rgtrUpInstCd; // 등록상위기관코드
    private String rgtrUpInstCdNm; // 등록상위기관명
    private String rgtrHghrkInstCd; // 등록최상위기관코드
    private String rgtrHghrkInstCdNm; // 등록최상위기관명
    private String zipCd; // 우편번호
    private String plcyMajorCd; // 정책전공코드
    private String jobCd; // 직업코드
    private String schoolCd; // 학교코드
    private String sbizCd; // 특수사업코드
}