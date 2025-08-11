package org.project.soar.model.usertag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.model.tag.Tag;
import org.project.soar.model.usertag.UserTag;
import org.project.soar.model.usertag.dto.UserTagRequest;
import org.project.soar.model.usertag.dto.UserTagResponse;
import org.project.soar.model.tag.repository.TagRepository;
import org.project.soar.model.usertag.repository.UserTagRepository;
import org.project.soar.model.user.User;
import org.project.soar.model.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserTagServiceImpl implements UserTagService {
    private final UserTagRepository userTagRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public UserTagResponse setUserTag(UserTagRequest userTagRequest) {
        Long userId = userTagRequest.getUserId();
        User newUser = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 기존 태그 삭제
        List<UserTag> existingTags = userTagRepository.findByUser(newUser);
        userTagRepository.deleteAll(existingTags);

        // 새로 저장
        List<UserTag> userTags = userTagRequest.getTagId()
                .stream()
                .map(tagId -> new UserTag(newUser, tagRepository.findById(tagId)
                        .orElseThrow(()-> new IllegalArgumentException("해당 태그가 존재하지 않습니다."))))
                .collect(Collectors.toList());

        userTagRepository.saveAll(userTags);
        log.info("saved: " + userTags.size());
        List<Tag> tags = userTagRepository.findAllTagByUserId(newUser.getUserId());
        UserTagResponse result = new UserTagResponse(userId, tags);
        return result;
    }

    public List<UserTagResponse> findAllUserTags() {
        List<User> users = userRepository.findAll();
        log.info("Found {} users", users.size());

        List<UserTagResponse> result = users.stream()
                .map(user -> {
                    List<Tag> tags = userTagRepository.findAllTagByUserId(user.getUserId());
                    return new UserTagResponse(user.getUserId(), tags);
                })
                .collect(Collectors.toList());
        return result;
    }

    public UserTagResponse findUserTagByUserId(Long userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));;
        List<Tag> tags = userTagRepository.findAllTagByUserId(user.getUserId());
                    return new UserTagResponse(user.getUserId(), tags);
    }

    public String getUserResidence(User user) {
        return userTagRepository.findResidenceTagsByUser(user).stream()
                .findFirst()
                .map(ut -> ut.getTag().getTagName()) // 태그명(거주지역명)
                .orElse(null); // 없으면 null
    }
}
