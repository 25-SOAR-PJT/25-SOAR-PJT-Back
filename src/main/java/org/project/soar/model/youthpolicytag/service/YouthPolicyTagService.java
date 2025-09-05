package org.project.soar.model.youthpolicytag.service;

import org.project.soar.model.tag.dto.YouthPolicyTagsResponse;
import org.project.soar.model.user.User;
import org.project.soar.model.youthpolicytag.YouthPolicyTag;
// import org.project.soar.model.youthpolicytag.controller.YouthPolicyTagRequest;
import org.project.soar.model.youthpolicytag.dto.FindYouthPolicyByTagResponse;
import org.project.soar.model.youthpolicytag.dto.YouthPolicyTagRequest;
import org.project.soar.model.youthpolicytag.dto.YouthPolicyTagResponse;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface YouthPolicyTagService {
    List<YouthPolicyTagResponse> getAllYouthPolicyTag();
    YouthPolicyTag setYouthPolicyTag(YouthPolicyTagResponse youthPolicyTagResponse);
    FindYouthPolicyByTagResponse getYouthPolicyTagByTagId(Long tagId);
    List<YouthPolicyTagResponse> createYouthPolicyTag(YouthPolicyTagRequest youthPolicyTagRequest);
    YouthPolicyTagsResponse getAllYouthPolicyByTagIds(List<Long> tagIds);
    YouthPolicyTagsResponse getYouthPolicyByUser(User user);
}
