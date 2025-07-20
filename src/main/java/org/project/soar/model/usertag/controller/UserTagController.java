package org.project.soar.model.usertag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.soar.global.api.ApiResponse;
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
    //유저 태그 할당, 수정
    @PostMapping("/")
    @Operation(summary = "유저-태그 할당, 수정", description = "기존의 유저-태그를 삭제하고, 새로운 유저-태그를 할당합니다.")
    public ResponseEntity<ApiResponse<UserTagResponse>> createUserTag(@RequestBody UserTagRequest userTagRequest){
        UserTagResponse result = userTagService.setUserTag(userTagRequest);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    //유저 태그 읽기(전체, 특정유저)
    @GetMapping("/all")
    @Operation(summary = "유저-태그 전체 조회", description = "모든 유저-태그를 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserTagResponse>>> getAllUserTags() {
        List<UserTagResponse> result = userTagService.findAllUserTags();
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "유저-태그 조회", description = "특정 유저의 모든 유저-태그를 조회합니다.")
    public ResponseEntity<ApiResponse<UserTagResponse>> getUserTagByUserId(@PathVariable("userId") Long userId) {
        log.info(userId.toString());
        UserTagResponse result = userTagService.findUserTagByUserId(userId);
        return ResponseEntity.ok(ApiResponse.createSuccess(result));
    }



}
