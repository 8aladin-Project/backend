package potato.backend.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오류 응답")
public record ErrorResponse(
    @Schema(description = "오류 코드 이름")
    String errorCodeName,
    @Schema(description = "오류 메시지")
    String errorMessage
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String errorMessage) {
        return new ErrorResponse(errorCode.name(), errorMessage);
    }
}
