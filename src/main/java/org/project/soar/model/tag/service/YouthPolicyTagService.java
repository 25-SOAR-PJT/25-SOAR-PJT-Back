package org.project.soar.model.tag.service;

import org.project.soar.model.tag.YouthPolicyTag;
import org.project.soar.model.tag.dto.YouthPolicyTagResponse;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface YouthPolicyTagService {
    public List<YouthPolicyTagResponse> getAllYouthPolicyTag();
    public YouthPolicyTag setYouthPolicyTag(YouthPolicyTagResponse youthPolicyTagResponse);
}
