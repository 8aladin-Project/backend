package potato.backend.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import potato.backend.domain.chat.domain.ChatRoom;
import potato.backend.domain.user.domain.Member;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 판매자와 구매자를 기준으로 채팅방을 조회하는 메서드
     * @param seller
     * @param buyer
     * @return
     */
    @Query("select cr from ChatRoom cr where cr.seller = :seller and cr.buyer = :buyer")
    Optional<ChatRoom> findByParticipants(@Param("seller") Member seller, @Param("buyer") Member buyer);


    /**
     * memberId를 기준으로 채팅방 목록을 조회하는 메서드
     * @param memberId
     * @return
     */
    @Query("select cr from ChatRoom cr where cr.seller.id = :memberId or cr.buyer.id = :memberId")
    List<ChatRoom> findAllByMemberId(@Param("memberId") Long memberId);
}
