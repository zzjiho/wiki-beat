package wiki.api;

import wiki.code.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RestApiResponse<T> {

    private String httpStatus;

    private String message;

    private T data;

    public static <T> RestApiResponse<T> success(T data) {
        return new RestApiResponse<>("200", "Request processed successfully", data);
    }

    public static <T> RestApiResponse<T> error(ErrorCode errorCode, String message) {
        return new RestApiResponse<>(errorCode.getCode(), message, null);
    }
}

