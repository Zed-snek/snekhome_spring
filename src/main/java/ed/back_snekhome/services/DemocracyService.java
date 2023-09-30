package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.democracy.GeneralDemocracyDto;
import ed.back_snekhome.dto.communityDTOs.democracy.ProgressDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.repositories.community.CommunityRoleRepository;
import ed.back_snekhome.repositories.community.MembershipRepository;
import ed.back_snekhome.repositories.communityDemocracy.CandidateRepository;
import ed.back_snekhome.repositories.communityDemocracy.ElectionsRepository;
import ed.back_snekhome.repositories.communityDemocracy.VoteRepository;
import ed.back_snekhome.repositories.post.CommentaryRatingRepository;
import ed.back_snekhome.repositories.post.PostRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@Service
@RequiredArgsConstructor
public class DemocracyService {


    private final CandidateRepository candidateRepository;
    private final ElectionsRepository electionsRepository;
    private final VoteRepository voteRepository;
    private final PostRatingRepository postRatingRepository;
    private final CommentaryRatingRepository commentaryRatingRepository;
    private final MembershipRepository membershipRepository;
    private final CommunityRoleRepository communityRoleRepository;

    private final MembershipService membershipService;
    private final UserMethodsService userMethodsService;
    private final CommunityMethodsService communityMethodsService;


    private void throwErrIfNotDemocracy(Community community) {
        if (community.getType() != CommunityType.DEMOCRACY)
            throw new BadRequestException("Community is not democracy");
    }

    private CommunityRole getCitizenRole(Community community) {
        return communityRoleRepository.findTopByCommunityAndIsCitizen(community, true)
                .orElseThrow(() -> new BadRequestException("Role is not found"));
    }

    public int getRating(Community community, UserEntity user) {
        return postRatingRepository.countAllByCommunityAndUser(community, user) +
                commentaryRatingRepository.countAllByCommunityAndUser(community, user);
    }

    public int getDaysAfterJoining(Membership membership) {
        return (int) ChronoUnit.DAYS.between(membership.getJoined(), LocalDate.now());
    }

    public boolean isCitizenRight(Community community, UserEntity user) {
        var optionalMembership = membershipService
                .getOptionalMembershipOfUser(community, user);

        if (optionalMembership.isPresent()) {
            var membership = optionalMembership.get();
            var citizenParameters = community.getCitizenParameters();
            if (getDaysAfterJoining(membership) >= citizenParameters.getDays()
                    && getRating(community, membership.getUser()) >= citizenParameters.getRating()
            ) {
                if (optionalMembership.map(Membership::getRole).isEmpty()) { //if citizen rights, but user doesn't have a role, grants him a role
                    membership.setRole(getCitizenRole(community));
                    membershipRepository.save(membership);
                }
                return true;
            }
        }
        return false;
    }



}
