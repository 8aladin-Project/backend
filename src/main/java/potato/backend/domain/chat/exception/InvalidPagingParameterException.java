package potato.backend.domain.chat.exception;

import potato.backend.global.exception.ErrorCode;

/**
 * 페이징 파라미터가 유효하지 않을 때 발생하는 예외
 */
public class InvalidPagingParameterException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidPagingParameterException(String message) {
        super(message);
        this.errorCode = ErrorCode.INVALID_PAGING_PARAMETER;
    }

    public InvalidPagingParameterException() {
        super(ErrorCode.INVALID_PAGING_PARAMETER.getMessage());
        this.errorCode = ErrorCode.INVALID_PAGING_PARAMETER;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
