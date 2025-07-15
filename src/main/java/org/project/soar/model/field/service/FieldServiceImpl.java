package org.project.soar.model.field.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.model.field.Field;
import org.project.soar.model.field.dto.FieldResponse;
import org.project.soar.model.field.repository.FieldRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldServiceImpl implements FieldService{
    public final FieldRepository fieldRepository;
    @Override
    public List<FieldResponse> setFieldList() {
        fieldRepository.deleteAll();
        List<String> names = List.of("일자리", "주거", "교육", "복지문화", "가구상황", "취업상태", "연령대", "추가지원조건" ,"소득구간");
        List<Field> fields = names.stream()
                .map(name -> new Field(name))
                .collect(Collectors.toList());
        fieldRepository.saveAll(fields);
        log.info("Field 생성 완료");
        return getFieldList();
    }

    @Override
    public List<FieldResponse> getFieldList() {
        List<Field> fields = fieldRepository.findAll();
        List<FieldResponse> result = fields.stream()
                                            .map(field -> FieldResponse.builder()
                                                                        .fieldId(field.getFieldId())
                                                                        .fieldName(field.getFieldName())
                                                    .build())
                                            .collect(Collectors.toList());
        return result;
    }
}