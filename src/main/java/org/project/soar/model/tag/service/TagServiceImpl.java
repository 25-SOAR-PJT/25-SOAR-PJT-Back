package org.project.soar.model.tag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.model.field.Field;
import org.project.soar.model.field.repository.FieldRepository;
import org.project.soar.model.field.service.FieldService;
import org.project.soar.model.tag.Tag;
import org.project.soar.model.tag.dto.TagResponse;
import org.project.soar.model.tag.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TagServiceImpl implements TagService{

    public final TagRepository tagRepository;
    public final FieldRepository fieldRepository;
    public final FieldService fieldService;

    @Override
    public List<TagResponse> getAllTagList() {
        List<Tag> tags = tagRepository.findAll();
        List<TagResponse> result = tags.stream()
                                        .map(tag -> TagResponse.builder()
                                                            .tagId(tag.getTagId())
                                                            .tagName(tag.getTagName())
                                                            .fieldId(tag.getField().getFieldId())
                                                            .build())
                                        .collect(Collectors.toList());
        return result;
    }

    @Override
    public List<TagResponse> setTagList() {
        if (fieldRepository.findAll().isEmpty()){
            fieldService.setFieldList();
        }
        List<Field> fields = fieldRepository.findAll();

        tagRepository.deleteAll();

        List<String> tags_job = List.of("취_창업 컨설팅", "면접지원", "멘토_멘티", "인턴십&연수&공고", "창업 자금 지원", "기타");
        List<String> tags_house = List.of("신혼부부주거지원", "청년주거지원", "여성주거지원", "기타");
        List<String> tags_study = List.of("주거 교육", "안전 교육", "예술 교육", "IT_마케팅 교육", "인문 교육", "요리 교육", "취_창업 교육", "금융 교육", "독서실 지원", "응시료&수강료 지원", "장학금&학자금 제출", "기타");
        List<String> tags_care = List.of("문화행사", "심리상담", "의료지원", "공간지원", "물품지원", "소상공인지원", "장애인지원", "양육지원", "교통비지원", "병역지원", "기타");
        List<String> tags_familySit = List.of("저소득", "장애인", "한부모_조손", "다자녀", "다문화_탈북민", "보훈대상자");
        List<String> tags_employed = List.of("재직자", "자영업자", "미취업자", "프리랜서", "일용근로자", "(예비)창업자", "단기근로자", "영농종사자", "제한없음");
        List<String> tags_ages = List.of("19-24", "25-29", "30-34", "35-39");
        List<String> tags_additional = List.of("지역인재", "중소기업", "기혼자", "군인", "농업인", "한부모가정", "기초생활수급자", "장애인");
        List<String> tags_income = List.of("기초생활수급자","무소득 또는 월 50만원 이하","월 소득 50~150만원","월소득 150~250만원", "기타","제한없음");

        List<List> array_tags = List.of(tags_job, tags_house, tags_study, tags_care, tags_familySit, tags_employed, tags_ages, tags_additional, tags_income);

        for (int i = 0; i < array_tags.size(); i++) {
            List<String> tags = array_tags.get(i);
            Field field = fields.get(i);

            List<Tag> result = tags.stream()
                    .map(tag -> new Tag(tag, field))
                    .collect(Collectors.toList());

            tagRepository.saveAll(result);
        }
        log.info("Tag 생성 완료");

        List<TagResponse> returnValue = getAllTagList();
        return returnValue;
    }
}
