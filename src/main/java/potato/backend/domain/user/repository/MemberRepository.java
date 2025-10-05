// 해당 파일은 임시 파일입니다. MemberRepository가 다른 브랜치에서 구현되면 삭제하겠습니다.

package potato.backend.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import potato.backend.domain.user.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
