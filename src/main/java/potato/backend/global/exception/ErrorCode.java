package potato.backend.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않았습니다"),

    INVALID_JSON(HttpStatus.BAD_REQUEST, "유효하지 않은 JSON 형식입니다"),
    INVALID_ENUM(HttpStatus.BAD_REQUEST, "유효하지 않은 Enum 값입니다"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "데이터 무결성 위반입니다"),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다"),

    // 커리큘럼
    CURRICULUM_NOT_FOUND(HttpStatus.NOT_FOUND, "커리큘럼을 찾을 수 없습니다"),
    CURRICULUM_ALREADY_ENROLLED(HttpStatus.CONFLICT, "이미 등록된 커리큘럼입니다"),
    CURRICULUM_ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 정보를 찾을 수 없습니다"),
    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "챕터를 찾을 수 없습니다"),
    CHAPTER_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "챕터 진행 정보를 찾을 수 없습니다"),

    // AI 튜터
    CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다"),
    MESSAGE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메시지 생성에 실패했습니다"),
    INVALID_MESSAGE_ROLE(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 메시지 역할입니다"),


    // Image
    INVALID_IMAGE(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 파일입니다"),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다"),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 파일 크기가 제한을 초과했습니다"),

    // Chat
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다"),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다"),
    CHAT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅 사용자를 찾을 수 없습니다"),
    CHAT_PARTICIPANT_NOT_FOUND(HttpStatus.FORBIDDEN, "채팅방에 참여할 권한이 없습니다"),
    INVALID_CHAT_ROOM_PARTICIPANTS(HttpStatus.BAD_REQUEST, "채팅방 참가자가 유효하지 않습니다"),
    CHAT_MESSAGE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 메시지입니다"),

    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "유효하지 않은 인자입니다")
    ;
    private final HttpStatus httpStatus;
    private final String message;
}
