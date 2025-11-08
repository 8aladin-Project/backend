package potato.backend.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import potato.backend.domain.chat.domain.ChatMessage;
import potato.backend.domain.chat.domain.ChatRoom;
import potato.backend.domain.user.domain.Member;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방에서 읽지 않은 메시지들을 조회 (특정 사용자가 보낸 메시지 제외)
     * @param chatRoom 채팅방
     * @param isRead 읽음 여부
     * @param sender 제외할 사용자
     * @return 읽지 않은 메시지 리스트
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom AND cm.isRead = :isRead AND cm.sender != :sender")
    List<ChatMessage> findByChatRoomAndIsReadAndSenderNot(@Param("chatRoom") ChatRoom chatRoom, @Param("isRead") boolean isRead, @Param("sender") Member sender);

    /**
     * 읽지 않은 메시지 개수 조회 (특정 사용자가 보낸 메시지 제외)
     * @param isRead 읽음 여부
     * @param sender 제외할 사용자
     * @return 읽지 않은 메시지 개수
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.isRead = :isRead AND cm.sender != :sender")
    long countByIsReadAndSenderNot(@Param("isRead") boolean isRead, @Param("sender") Member sender);

    /**
     * 채팅방에서 읽지 않은 메시지 개수 조회 (특정 사용자가 보낸 메시지 제외)
     * @param chatRoom 채팅방
     * @param isRead 읽음 여부
     * @param sender 제외할 사용자
     * @return 읽지 않은 메시지 개수
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom AND cm.isRead = :isRead AND cm.sender != :sender")
    long countByChatRoomAndIsReadAndSenderNot(@Param("chatRoom") ChatRoom chatRoom, @Param("isRead") boolean isRead, @Param("sender") Member sender);

    /**
     * 특정 채팅방의 모든 메시지 조회
     * @param chatRoom 채팅방
     * @return 메시지 리스트
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByChatRoomOrderBySentAtAsc(@Param("chatRoom") ChatRoom chatRoom);

    /**
     * 특정 사용자가 참여한 채팅방의 읽지 않은 메시지 개수 조회
     * @param sender 메시지 보낸 사용자
     * @param isRead 읽음 여부
     * @return 읽지 않은 메시지 개수
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.sender = :sender AND cm.isRead = :isRead")
    long countBySenderAndIsRead(@Param("sender") Member sender, @Param("isRead") boolean isRead);

    /**
     * 특정 사용자가 참여한 채팅방에서 읽지 않은 메시지 개수 조회 (사용자가 보낸 메시지 제외)
     * @param member 사용자
     * @return 읽지 않은 메시지 개수
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
           "WHERE (cm.chatRoom.seller = :member OR cm.chatRoom.buyer = :member) " +
           "AND cm.isRead = false " +
           "AND cm.sender != :member")
    long countUnreadMessagesForMember(@Param("member") Member member);

    /**
     * 채팅방의 메시지 목록을 페이징하여 조회 (커서 기반)
     * @param chatRoom 채팅방
     * @param beforeMessageId 기준 메시지 ID (이 ID보다 오래된 메시지만 조회)
     * @param limit 조회할 메시지 개수
     * @return 메시지 리스트
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom " +
           "AND (:beforeMessageId IS NULL OR cm.id < :beforeMessageId) " +
           "ORDER BY cm.sentAt DESC")
    List<ChatMessage> findByChatRoomWithPaging(@Param("chatRoom") ChatRoom chatRoom,
                                               @Param("beforeMessageId") Long beforeMessageId,
                                               org.springframework.data.domain.Pageable pageable);

    /**
     * 채팅방의 메시지 개수 조회 (페이징용)
     * @param chatRoom 채팅방
     * @param beforeMessageId 기준 메시지 ID
     * @return 메시지 개수
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom " +
           "AND (:beforeMessageId IS NULL OR cm.id < :beforeMessageId)")
    long countByChatRoomWithPaging(@Param("chatRoom") ChatRoom chatRoom,
                                   @Param("beforeMessageId") Long beforeMessageId);

}