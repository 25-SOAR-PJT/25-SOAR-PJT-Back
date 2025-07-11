package org.project.soar.model.tag.service;

import lombok.RequiredArgsConstructor;
import org.project.soar.model.tag.YouthPolicyTag;
import org.project.soar.model.tag.dto.YouthPolicyTagResponse;
import org.project.soar.model.tag.repository.YouthPolicyTagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouthPolicyTagServiceImpl implements YouthPolicyTagService{

    YouthPolicyTagRepository youthPolicyTagRepository;
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
    public YouthPolicyTag setYouthPolicyTag() {
        //TODO : 지피티에서 얻어온 태그 이걸로 넣기
        return null;
    }
}
