package ed.back_snekhome.services;

import ed.back_snekhome.repositories.communityDemocracy.CandidateRepository;
import ed.back_snekhome.repositories.communityDemocracy.ElectionsRepository;
import ed.back_snekhome.repositories.communityDemocracy.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemocracyService {


    private final CandidateRepository candidateRepository;
    private final ElectionsRepository electionsRepository;
    private final VoteRepository voteRepository;

}
