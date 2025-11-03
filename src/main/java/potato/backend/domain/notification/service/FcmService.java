package potato.backend.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import potato.backend.domain.user.domain.Member;

/**
 * Firebase Cloud Messaging (FCM) 알림 전송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;

    /**
     * 채팅 메시지 알림 전송
     * @param recipient 수신자 (Member 객체)
     * @param senderName 발신자 이름
     * @param messageContent 메시지 내용
     * @param roomId 채팅방 ID
     * @return 전송 성공 여부
     */
    public boolean sendChatNotification(Member recipient, String senderName, String messageContent, Long roomId) {
        // 푸시 알림이 비활성화된 사용자면 전송하지 않음
        if (recipient.getPushNotificationEnabled() == null || !recipient.getPushNotificationEnabled()) {
            log.debug("푸시 알림이 비활성화된 사용자: memberId={}", recipient.getId());
            return false;
        }

        // FCM 토큰이 없으면 전송 불가
        String fcmToken = recipient.getFcmToken();
        if (fcmToken == null || fcmToken.isEmpty()) {
            log.debug("FCM 토큰이 없는 사용자: memberId={}", recipient.getId());
            return false;
        }

        try {
            // 메시지 내용이 너무 길면 잘라냄 (FCM 제한: 1000자)
            String truncatedContent = truncateMessage(messageContent, 50);

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(senderName + "님의 메시지")
                            .setBody(truncatedContent)
                            .build())
                    // 클라이언트에서 알림 클릭 시 채팅방으로 이동할 수 있도록 데이터 추가
                    .putData("type", "chat")
                    .putData("roomId", String.valueOf(roomId))
                    .putData("senderName", senderName)
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("FCM 알림 전송 성공: recipientId={}, roomId={}, response={}", 
                    recipient.getId(), roomId, response);
            return true;

        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 전송 실패: recipientId={}, roomId={}, error={}", 
                    recipient.getId(), roomId, e.getMessage(), e);
            
            // 유효하지 않은 토큰인 경우 (사용자가 앱을 삭제했거나 토큰이 만료된 경우)
            if (e.getErrorCode() != null && (
                    e.getErrorCode().equals("invalid-registration-token") ||
                    e.getErrorCode().equals("registration-token-not-registered"))) {
                log.warn("유효하지 않은 FCM 토큰: recipientId={}, fcmToken={}", 
                        recipient.getId(), fcmToken);
                // TODO: Member 엔티티의 fcmToken을 null로 업데이트하는 로직 추가 가능
            }
            return false;
        } catch (Exception e) {
            log.error("FCM 알림 전송 중 예상치 못한 오류: recipientId={}, roomId={}", 
                    recipient.getId(), roomId, e);
            return false;
        }
    }

    /**
     * 메시지 내용을 지정된 길이로 잘라냄
     * @param message 원본 메시지
     * @param maxLength 최대 길이
     * @return 잘린 메시지
     */
    private String truncateMessage(String message, int maxLength) {
        if (message == null) {
            return "";
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...";
    }
}

