package ed.back_snekhome.repositories;

import ed.back_snekhome.entities.relations.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

}
