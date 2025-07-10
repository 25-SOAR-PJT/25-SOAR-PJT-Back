package org.project.soar.model.field.service;

import org.project.soar.model.field.dto.FieldResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FieldService {
    List<FieldResponse> setFieldList();

    List<FieldResponse> getFieldList();
}
