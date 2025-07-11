package org.project.soar.model.tag.service;

import org.project.soar.model.tag.dto.TagResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TagService {
    List<TagResponse> getAllTagList();

    List<TagResponse> setTagList();
}
