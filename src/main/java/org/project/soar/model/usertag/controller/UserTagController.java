package org.project.soar.model.usertag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.config.TokenProvider;
import org.project.soar.global.api.ApiResponse;
import org.project.soar.model.user.User;
import org.project.soar.model.usertag.dto.UserTagRequest;
import org.project.soar.model.usertag.dto.UserTagResponse;
import org.project.soar.model.usertag.service.UserTagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user-tag")
@RequiredArgsConstructor
@Tag(name= "UserTag", description = "UserTag API")
public class UserTagController {
    private final UserTagService userTagService;
    private final TokenProvider tokenProvider;
    //유저 태그 할당, 수정
    @PostMapping("/")
    @Operation(summary = "유저-태그 할당, 수정", description = "기존의 유저-태그를 삭제하고, 새로운 유저-태그를 할당합니다.")
    public ResponseEntity<ApiResponse<?>> createUserTag(HttpServletRequest request, @RequestBody UserTagRequest userTagRequest){
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        UserTagResponse result = userTagService.setUserTag(user, userTagRequest);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    //유저 태그 읽기(전체, 특정유저)
    @GetMapping("/all")
    @Operation(summary = "유저-태그 전체 조회", description = "모든 유저-태그를 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserTagResponse>>> getAllUserTags() {
        List<UserTagResponse> result = userTagService.findAllUserTags();
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/user")
    @Operation(summary = "유저-태그 조회", description = "특정 유저의 모든 유저-태그를 조회합니다.")
    public ResponseEntity<ApiResponse<?>> getUserTagByUserId(HttpServletRequest request) {
        User user = tokenProvider.getUserFromRequest(request);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.createError("사용자 인증에 실패했습니다."));
        }
        UserTagResponse result = userTagService.findUserTagByUserId(user.getUserId());
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }
}
