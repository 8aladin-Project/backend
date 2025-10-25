package potato.backend.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import potato.backend.domain.chat.domain.ChatRoom;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.user.domain.Member;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 판매자와 구매자를 기준으로 채팅방을 조회하는 메서드
     * @param seller 판매자(아이디)
     * @param buyer 구매자(아이디)
     * @return 채팅방
     */
    @Query("select cr from ChatRoom cr where cr.seller = :seller and cr.buyer = :buyer")
    Optional<ChatRoom> findByParticipants(@Param("seller") Member seller, @Param("buyer") Member buyer);

    /**
     * 판매자, 구매자, 상품을 기준으로 채팅방을 조회하는 메서드
     * @param seller 판매자
     * @param buyer 구매자
     * @param product 상품
     * @return 채팅방
     */
    @Query("select cr from ChatRoom cr where cr.seller = :seller and cr.buyer = :buyer and cr.product = :product")
    Optional<ChatRoom> findByParticipantsAndProduct(@Param("seller") Member seller, @Param("buyer") Member buyer, @Param("product") Product product);


    /**
     * memberId를 기준으로 채팅방 목록을 조회하는 메서드
     * @param memberId
     * @return 해당 유저(판매자 혹은 구매자)가 참여한 채팅방 목록
     */
    @Query("select cr from ChatRoom cr where cr.seller.id = :memberId or cr.buyer.id = :memberId")
    List<ChatRoom> findAllByMemberId(@Param("memberId") Long memberId);
}
