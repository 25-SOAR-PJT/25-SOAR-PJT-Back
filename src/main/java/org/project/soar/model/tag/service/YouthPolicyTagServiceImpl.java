package org.project.soar.model.tag.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.field.Field;
import org.project.soar.model.tag.Tag;
import org.project.soar.model.tag.YouthPolicyTag;
import org.project.soar.model.tag.dto.YouthPolicyTagResponse;
import org.project.soar.model.tag.repository.TagRepository;
import org.project.soar.model.tag.repository.YouthPolicyTagRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouthPolicyTagServiceImpl implements YouthPolicyTagService{
    public final YouthPolicyRepository youthPolicyRepository;
    public final TagRepository tagRepository;
    public final YouthPolicyTagRepository youthPolicyTagRepository;
    @Override
    public List<YouthPolicyTagResponse> getAllYouthPolicyTag() {
        List<YouthPolicyTagResponse> result =
                youthPolicyTagRepository.findAll().stream()
                        .map(policyTag -> new YouthPolicyTagResponse(
                                policyTag.getYouthPolicy().getPolicyId(),
                                policyTag.getTag().getTagId()))
                        .collect(Collectors.toList());
        return result;
    }

    @Override
    public YouthPolicyTag setYouthPolicyTag(YouthPolicyTagResponse youthPolicyTag) {
        YouthPolicy myYouthPolicy = youthPolicyRepository.findByPolicyId(youthPolicyTag.getPolicyId());
        Tag myTag = tagRepository.findByTagId(youthPolicyTag.getTagId());
        Field myField = myTag.getField();
        YouthPolicyTag newYouthPolicyTag = new YouthPolicyTag(myYouthPolicy, myTag, myField);
        youthPolicyTagRepository.save(newYouthPolicyTag);
        return newYouthPolicyTag;
    }
}
