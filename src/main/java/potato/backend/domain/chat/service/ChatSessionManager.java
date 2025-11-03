package potato.backend.domain.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 세션 관리를 위한 컴포넌트
 * 특정 사용자가 특정 채팅방에 연결되어 있는지 추적합니다.
 */
@Slf4j
@Component
public class ChatSessionManager {

    // Key: roomId, Value: 해당 채팅방에 연결된 사용자 ID Set
    private final Map<Long, Set<Long>> roomSessions = new ConcurrentHashMap<>();

    /**
     * 사용자가 특정 채팅방에 연결되었을 때 호출
     * @param roomId 채팅방 ID
     * @param memberId 사용자 ID
     */
    public void addSession(Long roomId, Long memberId) {
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
                .add(memberId);
        log.debug("사용자가 채팅방에 연결됨: roomId={}, memberId={}", roomId, memberId);
    }

    /**
     * 사용자가 특정 채팅방에서 연결 해제되었을 때 호출
     * @param roomId 채팅방 ID
     * @param memberId 사용자 ID
     */
    public void removeSession(Long roomId, Long memberId) {
        Set<Long> members = roomSessions.get(roomId);
        if (members != null) {
            members.remove(memberId);
            if (members.isEmpty()) {
                roomSessions.remove(roomId);
            }
            log.debug("사용자가 채팅방에서 연결 해제됨: roomId={}, memberId={}", roomId, memberId);
        }
    }

    /**
     * 사용자가 특정 채팅방에 연결되어 있는지 확인
     * @param roomId 채팅방 ID
     * @param memberId 사용자 ID
     * @return 연결되어 있으면 true, 아니면 false
     */
    public boolean isUserConnected(Long roomId, Long memberId) {
        Set<Long> members = roomSessions.get(roomId);
        return members != null && members.contains(memberId);
    }

    /**
     * 특정 채팅방에 연결된 모든 사용자 ID 조회
     * @param roomId 채팅방 ID
     * @return 연결된 사용자 ID Set
     */
    public Set<Long> getConnectedUsers(Long roomId) {
        return roomSessions.getOrDefault(roomId, ConcurrentHashMap.newKeySet());
    }

    /**
     * 사용자가 모든 채팅방에서 연결 해제되었을 때 호출 (WebSocket 전체 연결 종료 시)
     * @param memberId 사용자 ID
     */
    public void removeAllSessions(Long memberId) {
        roomSessions.forEach((roomId, members) -> {
            if (members.remove(memberId)) {
                log.debug("사용자가 모든 채팅방에서 연결 해제됨: memberId={}", memberId);
            }
            if (members.isEmpty()) {
                roomSessions.remove(roomId);
            }
        });
    }
}

