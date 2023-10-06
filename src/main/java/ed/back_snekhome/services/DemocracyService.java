package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.GeneralDemocracyDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.Elections;
import ed.back_snekhome.entities.communityDemocracy.PresidencyData;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.enums.PresidencyDataType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.repositories.community.CommunityRepository;
import ed.back_snekhome.repositories.community.CommunityRoleRepository;
import ed.back_snekhome.repositories.community.MembershipRepository;
import ed.back_snekhome.repositories.communityDemocracy.PresidencyDataRepository;
import ed.back_snekhome.repositories.communityDemocracy.CandidateRepository;
import ed.back_snekhome.repositories.communityDemocracy.ElectionsRepository;
import ed.back_snekhome.repositories.communityDemocracy.VoteRepository;
import ed.back_snekhome.repositories.post.CommentaryRatingRepository;
import ed.back_snekhome.repositories.post.PostRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PresidencyDataRepository presidencyDataRepository;
    private final CommunityRepository communityRepository;

    private final MembershipMethodsService membershipMethodsService;
    private final UserMethodsService userMethodsService;
    private final CommunityMethodsService communityMethodsService;

    @Value("${democracy.elections.duration}")
    private int electionsDuration;

    private void throwErrIfNotDemocracy(Community community) {
        if (community.getType() != CommunityType.DEMOCRACY)
            throw new BadRequestException("Community is not democracy");
    }

    private CommunityRole getCitizenRole(Community community) {
        return communityRoleRepository.findTopByCommunityAndIsCitizen(community, true)
                .orElseThrow(() -> new BadRequestException("Role is not found"));
    }

    private int getRating(Community community, UserEntity user) {
        return postRatingRepository.countAllByCommunityAndUser(community, user) +
                commentaryRatingRepository.countAllByCommunityAndUser(community, user);
    }

    private int getDaysAfterJoining(Membership membership) {
        return (int) ChronoUnit.DAYS.between(membership.getJoined(), LocalDate.now());
    }

    public boolean isCitizenRight(Community community, UserEntity user) {
        var optionalMembership = membershipMethodsService
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

    private PresidencyData getPresidencyDataByCommunity(Community community) {
        return presidencyDataRepository.findById(community.getIdCommunity())
                .orElseThrow(() -> new BadRequestException("Community is not Democracy"));
    }

    private void clearPresidencyDataByCommunity(Community community) {
        var data = getPresidencyDataByCommunity(community);
        data.clearData();
        presidencyDataRepository.save(data);
    }

    public void addStatsToPresidency(Community community, PresidencyDataType type) {
        var data = getPresidencyDataByCommunity(community);
        switch (type) {
            case DELETED_POST -> data.addDeletedPosts();
            case BANNED_USER -> data.addBannedUsers();
            case BANNED_CITIZEN -> data.addBannedCitizens();
        }
        presidencyDataRepository.save(data);
    }


    private boolean processDemocracy(Elections elections) { //method to start/finish elections | works only when someone checks community
        var nowDate = LocalDate.now();
        if (elections.getEndDate().isBefore(nowDate)) {
            var community = elections.getCommunity();
            var winner = candidateRepository.findCandidateWithMostVotes(community);
            winner.ifPresent(candidate -> { //(if 0 votes, leaves the same president)
                updateElectionsDate(elections, candidate);
                community.setOwner(candidate.getUser());
            });
            clearPresidencyDataByCommunity(community);
            candidateRepository.makeAllCandidatesInActive(community);

            communityRepository.save(community);
            return false;
        }
        boolean isElectionsNow = elections.getStartDate().isAfter(nowDate);
        if (isElectionsNow && !elections.isActive()) { //if elections only have started
            elections.setActive(true);
            electionsRepository.save(elections);
            voteRepository.deleteAllByCommunity(elections.getCommunity());
        }
        return isElectionsNow;
    }

    @Transactional
    public GeneralDemocracyDto getGeneralDemocracyData(String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var user = userMethodsService.getCurrentUser();
        throwErrIfNotDemocracy(community);

        var optMembership = membershipMethodsService.getOptionalMembershipOfUser(community, user);
        communityMethodsService.throwErrIfNoAccessToCommunity(community, optMembership);
        var elections = community.getElections();

        var dtoBuilder = GeneralDemocracyDto.builder();

        boolean isElectionsNow = processDemocracy(elections);
        dtoBuilder
                .isElectionsNow(isElectionsNow)
                .electionsDate(isElectionsNow ? elections.getEndDate() : elections.getStartDate());

        var presidencyData = getPresidencyDataByCommunity(community);
        dtoBuilder.bannedUsersStats(presidencyData.getBannedUsers())
                .bannedCitizensStats(presidencyData.getBannedCitizens())
                .deletedPostsStats(presidencyData.getDeletedPosts())
                .citizenAmount(membershipRepository.countCitizensByCommunity(community));

        dtoBuilder.currentPresidentProgram(elections.getCurrentPresident().getProgram());

        if (optMembership.isPresent()) {
            var membership = optMembership.get();

            boolean isRight = isCitizenRight(community, user);
            dtoBuilder.isCitizenRight(isRight); // is citizen rights

            if (isRight) {
                candidateRepository.findTopByUserAndCommunity(user, community)
                        .ifPresentOrElse(
                                candidate -> {
                                    dtoBuilder.currentUserProgram(candidate.getProgram());
                                    dtoBuilder.isCurrentUserActiveCandidate(candidate.isActive());
                                },
                                () -> dtoBuilder.isCurrentUserActiveCandidate(false)
                        );
            }
            else {
                dtoBuilder
                        .currentUserRating(getRating(community, user))
                        .currentUserDays(getDaysAfterJoining(membership));
            }
        }

        return dtoBuilder.build();
    }

    private Elections updateElectionsDate(Elections elections, Candidate newPresident) {
        var date = LocalDate.now().plusDays(
                elections.getCommunity().getCitizenParameters().getElectionDays()
        );
        elections.setStartDate(date);
        elections.setEndDate(date.plusDays(electionsDuration));
        elections.setCurrentPresident(newPresident);
        return elections;
    }

    public void createElections(Candidate currentPresident, Community community) {
        var elections = new Elections();
        elections.setCommunity(community);
        electionsRepository.save(updateElectionsDate(elections, currentPresident));
    }

}
