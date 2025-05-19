package org.project.soar.global.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private String status;
    private T data;
    private String message;

    /**
     * 성공적으로 API 호출이 완료되었을 때 반환 (메시지 없음)
     * 
     * @param data API 호출 결과 데이터
     * @return ApiResponse<?>
     * @param <T> API 호출 결과 데이터 타입
     */
    public static <T> ApiResponse<T> createSuccess(T data) {
        return new ApiResponse<>(Status.SUCCESS, data, null);
    }

    /**
     * 성공적으로 API 호출이 완료되었을 때 반환 (메시지 포함)
     * 
     * @param data    API 호출 결과 데이터
     * @param <T>     API 호출 결과 데이터 타입
     * @param message API 호출 결과 메시지
     * @return ApiResponse<?>
     */
    public static <T> ApiResponse<T> createSuccessWithMessage(T data, String message) {
        return new ApiResponse<>(Status.SUCCESS, data, message);
    }

    /**
     * 성공적으로 API 호출이 완료되었을 때 반환 (데이터 없음)
     * 
     * @return ApiResponse<?>
     */
    public static ApiResponse<?> createSuccessWithNoContent(String s) {
        return new ApiResponse<>(Status.SUCCESS, null, null);
    }

    /**
     * Hibernate Validator에 의해 유효하지 않은 데이터로 인해 API 호출이 거부될때 반환
     * 
     * @param bindingResult 유효하지 않은 데이터
     * @return ApiResponse<?>
     */
    public static ApiResponse<?> createFail(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        List<ObjectError> allErrors = bindingResult.getAllErrors();
        for (ObjectError error : allErrors) {
            if (error instanceof FieldError) {
                errors.put(((FieldError) error).getField(), error.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        }
        return new ApiResponse<>(Status.FAIL, errors, null);
    }

    /**
     * 예외 발생으로 API 호출 실패 시 반환
     * 
     * @param message 예외 메시지
     * @return ApiResponse<?>
     */
    public static ApiResponse<?> createError(String message) {
        return new ApiResponse<>(Status.ERROR, null, message);
    }

    private ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }
}
