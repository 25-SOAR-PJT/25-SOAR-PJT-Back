package org.project.soar.model.tag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class YouthPolicyTagServiceImpl implements YouthPolicyTagService{
    public final YouthPolicyRepository youthPolicyRepository;
    public final YouthPolicyTagRepository youthPolicyTagRepository;
    public final TagService tagService;
    public final TagRepository tagRepository;

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
        YouthPolicyTag newYouthPolicyTag = new YouthPolicyTag();
        if (youthPolicyRepository.existsByPolicyId(youthPolicyTag.getPolicyId())){
            YouthPolicy myYouthPolicy = youthPolicyRepository.findByPolicyId(youthPolicyTag.getPolicyId());
            if (!tagRepository.existsByTagId(youthPolicyTag.getTagId())){
                log.info("[경고]태그가 존재하지 않습니다. 태그 id : {}", youthPolicyTag.getTagId());
                //default 태그 DB 할당 로직 실행
                log.info("[알림] 태그 DB 할당 로직을 실행합니다.");
                tagService.setTagList();
            }
                Tag myTag = tagRepository.findByTagId(youthPolicyTag.getTagId());
                Field myField = myTag.getField();
                newYouthPolicyTag.setYouthPolicy(myYouthPolicy);
                newYouthPolicyTag.setTag(myTag);
                newYouthPolicyTag.setField(myField);
                youthPolicyTagRepository.save(newYouthPolicyTag);


        }else{
            log.info("[경고]정책이 존재하지 않습니다 태그 id : {}", youthPolicyTag.getPolicyId(), youthPolicyTag.getTagId());
        }
        return newYouthPolicyTag;

    }
}
