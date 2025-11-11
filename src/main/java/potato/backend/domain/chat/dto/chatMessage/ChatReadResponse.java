package potato.backend.domain.chat.dto.chatMessage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatReadResponse {

    private boolean success;
    private Data data;

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Data {
        private int readCount;  // 읽음 처리된 메시지 개수
    }

    public static ChatReadResponse success(int readCount) {
        Data data = Data.builder()
                .readCount(readCount)
                .build();

        return ChatReadResponse.builder()
                .success(true)
                .data(data)
                .build();
    }

    // 기존 메서드들과의 호환성을 위한 메서드들
    public static ChatReadResponse ofMessage(Long messageId, Long memberId, boolean success) {
        Data data = Data.builder()
                .readCount(success ? 1 : 0)
                .build();

        return ChatReadResponse.builder()
                .success(success)
                .data(data)
                .build();
    }

    public static ChatReadResponse ofRoom(Long roomId, Long memberId, int readCount, boolean success) {
        Data data = Data.builder()
                .readCount(readCount)
                .build();

        return ChatReadResponse.builder()
                .success(success)
                .data(data)
                .build();
    }
}
