package org.project.soar.model.tag.service;

import org.project.soar.model.tag.YouthPolicyTag;
import org.project.soar.model.tag.dto.YouthPolicyTagResponse;

import java.util.List;

public interface YouthPolicyTagService {
    public List<YouthPolicyTagResponse> getAllYouthPolicyTag();
    public YouthPolicyTag setYouthPolicyTag();
}
