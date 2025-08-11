package org.project.soar.model.usertag.service;

import org.project.soar.model.user.User;
import org.project.soar.model.usertag.dto.UserTagRequest;
import org.project.soar.model.usertag.dto.UserTagResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserTagService {
    UserTagResponse setUserTag(User newUser, UserTagRequest userTagRequest);
    List<UserTagResponse> findAllUserTags();
    UserTagResponse findUserTagByUserId(Long userId);
    String getUserResidence(User user);
}
