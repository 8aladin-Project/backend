package potato.backend.global.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import potato.backend.domain.chat.exception.ChatMessageInvalidException;
import potato.backend.domain.chat.exception.ChatMessageNotFoundException;
import potato.backend.domain.chat.exception.ChatParticipantNotFoundException;
import potato.backend.domain.chat.exception.ChatRoomNotFoundException;
import potato.backend.domain.chat.exception.InvalidChatRoomParticipantsException;
import potato.backend.domain.chat.exception.MemberNotFoundException;
import potato.backend.domain.image.exception.ImageNotFoundException;
import potato.backend.domain.image.exception.ImageUploadException;
import potato.backend.domain.image.exception.InvalidImageException;
import potato.backend.domain.product.exception.ProductNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private void logByType(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());

        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\n")
                    .append("\tat ")
                    .append(element.getClassName())
                    .append(".")
                    .append(element.getMethodName())
                    .append("(")
                    .append(element.getFileName())
                    .append(":")
                    .append(element.getLineNumber())
                    .append(")");
        }

        if (e instanceof CustomException customException) {
            if (customException.getErrorCode().getHttpStatus().value() >= 500) {
                log.error(sb.toString());
            } else {
                log.warn(sb.toString());
            }
        } else if (e instanceof DataIntegrityViolationException
                || e instanceof MethodArgumentNotValidException
                || e instanceof HttpMessageNotReadableException) {
            log.warn(sb.toString());
        } else {
            log.error(sb.toString());
        }
    }

    // CustomException을 처리합니다
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        logByType(e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(ErrorResponse.of(e.getErrorCode()));
    }

    // JPA에서 발생하는 DataIntegrityViolationException을 처리합니다
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(ErrorCode.DATA_INTEGRITY_VIOLATION));
    }

    // 알 수 없는 예외를 처리합니다
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        logByType(e);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    // @Valid 어노테이션을 통한 검증에 실패할 시 실행됩니다
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logByType(e);

        List<FieldErrorDetail> fieldErrorDetails = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> FieldErrorDetail.of(
                        fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        ArgumentNotValidErrorResponse errorResponse =
                ArgumentNotValidErrorResponse.of(ErrorCode.BAD_REQUEST, fieldErrorDetails);

        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus()).body(errorResponse);
    }

    // JSON 형식 오류 또는 Enum 변환에 실패할 시 실행됩니다
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logByType(e);

        Throwable cause = e.getCause();

        // 1. JSON 형식 오류
        if (cause instanceof com.fasterxml.jackson.core.JsonParseException
                || cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException) {
            return ResponseEntity.status(ErrorCode.INVALID_JSON.getHttpStatus())
                    .body(ErrorResponse.of(ErrorCode.INVALID_JSON));
        }

        // 2. Enum 변환 오류
        if (cause instanceof IllegalArgumentException) {
            return ResponseEntity.status(ErrorCode.INVALID_ENUM.getHttpStatus())
                    .body(ErrorResponse.of(ErrorCode.INVALID_ENUM));
        }

        // 3. 그 외의 메시지 읽기 실패 오류
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST));
    }

    // 이미지 도메인 예외들
    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidImageException(InvalidImageException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ErrorResponse> handleImageUploadException(ImageUploadException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.IMAGE_UPLOAD_FAILED, e.getMessage()));
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<Void> handleImageNotFoundException(ImageNotFoundException e) {
        logByType(e);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Void> handleProductNotFoundException(ProductNotFoundException e) {
        logByType(e);
        return ResponseEntity.noContent().build();
    }

    // Chat 도메인 예외들
    @ExceptionHandler(ChatRoomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomNotFoundException(ChatRoomNotFoundException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.CHAT_ROOM_NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(ChatMessageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChatMessageNotFoundException(ChatMessageNotFoundException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.CHAT_MESSAGE_NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMemberNotFoundException(MemberNotFoundException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.CHAT_MEMBER_NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(ChatParticipantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChatParticipantNotFoundException(ChatParticipantNotFoundException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(InvalidChatRoomParticipantsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidChatRoomParticipantsException(InvalidChatRoomParticipantsException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_CHAT_ROOM_PARTICIPANTS, e.getMessage()));
    }

    @ExceptionHandler(ChatMessageInvalidException.class)
    public ResponseEntity<ErrorResponse> handleChatMessageInvalidException(ChatMessageInvalidException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.CHAT_MESSAGE_INVALID, e.getMessage()));
    }

    /* ===================== 업로드/일반 파라미터 예외 ===================== */

    // MaxUploadSizeExceededException은 ResponseEntityExceptionHandler에서 처리되므로 제거
    // 필요시 handleExceptionInternal을 override하여 커스터마이징

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logByType(e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_ARGUMENT, e.getMessage()));
    }
}
