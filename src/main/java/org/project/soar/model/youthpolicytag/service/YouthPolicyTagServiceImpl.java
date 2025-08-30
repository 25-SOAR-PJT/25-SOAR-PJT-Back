package org.project.soar.model.youthpolicytag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.model.tag.Tag;
import org.project.soar.model.tag.dto.YouthPolicyTagsResponse;
import org.project.soar.model.tag.service.TagService;
import org.project.soar.model.user.User;
import org.project.soar.model.usertag.repository.UserTagRepository;
import org.project.soar.model.youthpolicy.dto.YouthPolicyMainItemDto;
import org.project.soar.model.youthpolicy.repository.YouthPolicyBookmarkRepository;
import org.project.soar.model.youthpolicytag.YouthPolicyTag;
import org.project.soar.model.youthpolicytag.dto.FindYouthPolicyByTagResponse;
import org.project.soar.model.youthpolicytag.dto.YouthPolicyTagRequest;
import org.project.soar.model.youthpolicytag.dto.YouthPolicyTagResponse;
import org.project.soar.model.tag.repository.TagRepository;
import org.project.soar.model.youthpolicytag.repository.PolicyTagMatchProjection;
import org.project.soar.model.youthpolicytag.repository.YouthPolicyTagRepository;
import org.project.soar.model.youthpolicy.YouthPolicy;
import org.project.soar.model.youthpolicy.repository.YouthPolicyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public final UserTagRepository userTagRepository;
    private final YouthPolicyBookmarkRepository bookmarkRepository;

    @Override
    public List<YouthPolicyTagResponse> getAllYouthPolicyTag() {
        List<YouthPolicyTagResponse> result =
                youthPolicyTagRepository.findAll().stream()
                        .map(policyTag -> new YouthPolicyTagResponse(
                                policyTag.getYouthPolicy().getPolicyId(),
                                policyTag.getYouthPolicy().getPolicyName(),
                                policyTag.getTag().getTagId(),
                                policyTag.getTag().getTagName()))
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
                newYouthPolicyTag.setYouthPolicy(myYouthPolicy);
                newYouthPolicyTag.setTag(myTag);
                youthPolicyTagRepository.save(newYouthPolicyTag);

        }else{
            log.info("[경고]정책이 존재하지 않습니다 태그 id : {}", youthPolicyTag.getPolicyId(), youthPolicyTag.getTagId());
        }
        return newYouthPolicyTag;

    }

    @Override
    public FindYouthPolicyByTagResponse getYouthPolicyTagByTagId(Long tagId) {
        List<YouthPolicy> youthPolicies = youthPolicyTagRepository.findByTagId(tagId);
        youthPolicies.stream().map(youthPolicy -> youthPolicy.getPolicyId()).collect(Collectors.toList());
        return new FindYouthPolicyByTagResponse(
                tagId,
                tagRepository.findByTagId(tagId).getTagName(),
                youthPolicies.stream().map(youthPolicy -> youthPolicy.getPolicyId()).collect(Collectors.toList())
        );
    }

    @Override
    public List<YouthPolicyTagResponse> createYouthPolicyTag(YouthPolicyTagRequest youthPolicyTagRequest) {
        YouthPolicy myYouthPolicy = youthPolicyRepository.findById(youthPolicyTagRequest.getYouthPolicyId()).orElse(null);
        List<Long> tagIds = youthPolicyTagRequest.getTagIds();
        List<Tag> tags = tagIds.stream().map(tagId -> tagRepository.findById(tagId).orElse(null)).collect(Collectors.toList());
        return tags.stream().map(tag -> {
            YouthPolicyTag result = youthPolicyTagRepository.save(new YouthPolicyTag(myYouthPolicy, tag));
            return new YouthPolicyTagResponse(result.getYouthPolicy().getPolicyId(), result.getYouthPolicy().getPolicyName(), result.getTag().getTagId(), result.getTag().getTagName());
        }).collect(Collectors.toList());
    }

    @Override
    public YouthPolicyTagsResponse getAllYouthPolicyByTagIds(List<Long> tagIds) {
        List<Tag> tags = tagIds.stream().map(tagId -> tagRepository.findByTagId(tagId)).collect(Collectors.toList());
        List<YouthPolicy> youthPolicies = youthPolicyTagRepository.findByTagIds(tagIds);
        return new YouthPolicyTagsResponse(tags, youthPolicies);
    }

    @Override
    public YouthPolicyTagsResponse getYouthPolicyByUser(User user) {
        List<Tag> tags = userTagRepository.findAllTagByUserId(user.getUserId());
        List<Long> tagIds = tags.stream().map(tag -> tag.getTagId()).collect(Collectors.toList());
        return getAllYouthPolicyByTagIds(tagIds);
    }

    @Override
    public Page<YouthPolicyMainItemDto> multiTagSearchPrioritizedMain(
            User user, List<Long> tagIds, Pageable pageable) {

        if (tagIds == null || tagIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 1) 정책별 매칭된 태그 개수 조회
        List<PolicyTagMatchProjection> matches = youthPolicyTagRepository.findPolicyMatchCounts(tagIds);
        if (matches.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2) 정렬: matchCount DESC → createdAt DESC
        matches.sort((a, b) -> {
            int cmp = Long.compare(
                    b.getMatchCount() != null ? b.getMatchCount() : 0L,
                    a.getMatchCount() != null ? a.getMatchCount() : 0L
            );
            if (cmp != 0) return cmp;

            var ad = a.getYouthPolicy().getCreatedAt();
            var bd = b.getYouthPolicy().getCreatedAt();
            if (ad != null && bd != null) return bd.compareTo(ad);
            if (ad != null) return -1;
            if (bd != null) return 1;
            return 0;
        });

        // 3) 메모리 페이징
        int total = matches.size();
        int start = Math.min((int) pageable.getOffset(), total);
        int end   = Math.min(start + pageable.getPageSize(), total);
        List<PolicyTagMatchProjection> slice = (start < end) ? matches.subList(start, end) : List.of();

        // 4) 북마크 계산은 "현재 페이지(slice)" 대상만
        List<YouthPolicy> content = slice.stream()
                .map(PolicyTagMatchProjection::getYouthPolicy)
                .collect(Collectors.toList());
        List<String> ids = content.stream().map(YouthPolicy::getPolicyId).toList();
        List<String> bookmarkedIds = bookmarkRepository.findBookmarkedPolicyIds(user, ids);
        var bookmarkedSet = new java.util.HashSet<>(bookmarkedIds);

        // 5) DTO 매핑 (searchMulti와 동일 형태)
        List<YouthPolicyMainItemDto> dtoList = content.stream().map(p ->
                YouthPolicyMainItemDto.builder()
                        .policyId(p.getPolicyId())
                        .policyName(p.getPolicyName())
                        .policyKeyword(p.getPolicyKeyword())
                        .largeClassification(p.getLargeClassification())
                        .mediumClassification(p.getMediumClassification())
                        .supervisingInstName(p.getSupervisingInstName())
                        .dateLabel(p.getDateLabel())
                        .bookmarked(bookmarkedSet.contains(p.getPolicyId()))
                        .build()
        ).toList();

        return new PageImpl<>(dtoList, pageable, total);
    }


}
