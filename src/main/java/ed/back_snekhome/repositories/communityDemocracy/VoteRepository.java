package ed.back_snekhome.repositories.communityDemocracy;

import ed.back_snekhome.entities.communityDemocracy.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

}
