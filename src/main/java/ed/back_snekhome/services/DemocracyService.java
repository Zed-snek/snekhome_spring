package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.CandidateDto;
import ed.back_snekhome.dto.communityDTOs.CandidateListDto;
import ed.back_snekhome.dto.communityDTOs.GeneralDemocracyDto;
import ed.back_snekhome.dto.communityDTOs.NewCandidateDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.entities.communityDemocracy.*;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.enums.ElectionsStatus;
import ed.back_snekhome.enums.PresidencyDataType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.community.CommunityRepository;
import ed.back_snekhome.repositories.community.CommunityRoleRepository;
import ed.back_snekhome.repositories.community.MembershipRepository;
import ed.back_snekhome.repositories.democracy.*;
import ed.back_snekhome.repositories.post.CommentaryRatingRepository;
import ed.back_snekhome.repositories.post.PostRatingRepository;
import ed.back_snekhome.helperComponents.CommunityHelper;
import ed.back_snekhome.helperComponents.MembershipHelper;
import ed.back_snekhome.helperComponents.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


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
    private final ElectionsParticipationRepository electionsParticipationRepository;

    private final NotificationService notificationService;
    private final MembershipHelper membershipHelper;
    private final UserHelper userHelper;
    private final CommunityHelper communityHelper;

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
        var optionalMembership = membershipHelper
                .getOptionalMembershipOfUser(community, user);

        if (optionalMembership.isPresent()) {
            var membership = optionalMembership.get();
            var optRole = optionalMembership.map(Membership::getRole);
            if (optRole.isPresent() && (optRole.get().isCreator() || optRole.get().isCitizen()))
                return true;

            var citizenParameters = community.getCitizenParameters();
            if (getDaysAfterJoining(membership) >= citizenParameters.getDays()
                    && getRating(community, membership.getUser()) >= citizenParameters.getRating()
            ) {
                if (optRole.isEmpty()) { //if citizen rights, but user doesn't have a role, grants him a role
                    membership.setRole(getCitizenRole(community));
                    membershipRepository.save(membership);
                }
                return true;
            }
        }
        return false;
    }

    public void throwErrIfNotCitizenRight(Community community, UserEntity user) {
        if (!isCitizenRight(community, user))
            throw new UnauthorizedException("No citizen rights");
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

    private ElectionsStatus getElectionsStatus(Elections elections) {
        var nowDate = LocalDate.now();
        if (elections.getEndDate().isBefore(nowDate))
            return ElectionsStatus.FINISHED;
        if (elections.getStartDate().isBefore(nowDate))
            return ElectionsStatus.IN_PROGRESS;
        return ElectionsStatus.NOT_STARTED;
    }

    private boolean processDemocracy(Elections elections) { //method to start/finish elections | works only when someone checks community
        var status = getElectionsStatus(elections);

        if (status == ElectionsStatus.FINISHED) {
            var community = elections.getCommunity();

            var winner = electionsParticipationRepository.findParticipantWithMostVotes(
                    community,
                    PageRequest.of(0, 1)
            );

            //(if 0 votes, leaves the same president)
            Candidate candidate;
            if (winner.isEmpty())
                candidate = elections.getCurrentPresident();
            else
                candidate = winner.get(0).getCandidate();

            community.setOwner(candidate.getUser());
            communityRepository.save(community);

            electionsParticipationRepository.updateElectionsParticipationWithVotes(elections.getElectionsNumber());
            voteRepository.deleteAllByElectionsParticipation_Elections(elections);

            //method sends to all current candidates, so it must be processed before "updateElectionsData()" method
            notificationService.createElectionsEndedNotification(community);

            electionsRepository.save(updateElectionsDate(elections, candidate));
            clearPresidencyDataByCommunity(community);

            return false;
        }

        return status == ElectionsStatus.IN_PROGRESS;
    }

    @Transactional
    public GeneralDemocracyDto getGeneralDemocracyData(String groupname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        throwErrIfNotDemocracy(community);

        Optional<Membership> optMembership;
        UserEntity user = null;
        if (userHelper.isContextUser()) {
            user = userHelper.getCurrentUser();
            optMembership = membershipHelper.getOptionalMembershipOfUser(community, user);
        }
        else {
            optMembership = Optional.empty();
        }
        communityHelper.throwErrIfNoAccessToCommunity(community, optMembership);

        var elections = community.getElections();

        var dtoBuilder = GeneralDemocracyDto.builder();

        if (optMembership.isPresent()) { //if not user, membership is empty
            var membership = optMembership.get();

            boolean isRight = isCitizenRight(community, user);
            dtoBuilder.isCitizenRight(isRight); // is citizen rights

            if (isRight) {
                candidateRepository.findTopByUserAndCommunity(user, community)
                        .ifPresent(candidate -> {
                                    dtoBuilder.currentUserProgram(candidate.getProgram());
                                    dtoBuilder.isCurrentUserActiveCandidate(
                                            electionsParticipationRepository
                                                    .findByCandidateIfActive(candidate)
                                                    .isPresent()
                                    );
                        });
            }
            else {
                dtoBuilder
                        .currentUserRating(getRating(community, user))
                        .currentUserDays(getDaysAfterJoining(membership));
            }
        }

        boolean isElectionsNow = processDemocracy(elections);
        dtoBuilder
                .isElectionsNow(isElectionsNow)
                .electionsDate(isElectionsNow ? elections.getEndDate() : elections.getStartDate());

        var presidencyData = getPresidencyDataByCommunity(community);
        dtoBuilder.bannedUsersStats(presidencyData.getBannedUsers())
                .bannedCitizensStats(presidencyData.getBannedCitizens())
                .deletedPostsStats(presidencyData.getDeletedPosts());

        dtoBuilder.currentPresidentProgram(elections.getCurrentPresident().getProgram());

        return dtoBuilder.build();
    }

    private Elections updateElectionsDate(Elections elections, Candidate newPresident) {
        var date = LocalDate.now().plusDays(
                elections.getCommunity().getCitizenParameters().getElectionDays()
        );
        elections.setStartDate(date);
        elections.setEndDate(date.plusDays(electionsDuration));
        elections.setCurrentPresident(newPresident);
        elections.setElectionsNumber(elections.getElectionsNumber() + 1);
        return elections;
    }

    public Elections createElections(Candidate currentPresident, Community community) {
        var elections = new Elections();
        elections.setCommunity(community);
        elections.setElectionsNumber(0);
        electionsRepository.save(updateElectionsDate(elections, currentPresident));
        return elections;
    }

    private Candidate findCandidateOrThrowErr(Community community, UserEntity user) {
        return candidateRepository.findTopByUserAndCommunity(user, community)
                .orElseThrow(() -> new EntityNotFoundException(user.getNickname() + " is not a candidate"));
    }

    private ElectionsParticipation findCurrentElectionsParticipationOrThrowErr(Candidate candidate, Community community) {
        return electionsParticipationRepository
                .findCurrentByElectionsAndCandidate(
                        community.getElections(),
                        candidate
                )
                .orElseThrow(() -> new BadRequestException("A candidate doesn't take part in elections"));
    }

    public void makeVote(String groupname, String candidateNickname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var userCandidate = userHelper.getUserByNicknameOrThrowErr(candidateNickname);
        var candidate = findCandidateOrThrowErr(community, userCandidate);
        var voter = userHelper.getCurrentUser();

        throwErrIfNotCitizenRight(community, voter);
        var electionsParticipation = findCurrentElectionsParticipationOrThrowErr(candidate, community);
        if (electionsParticipation.getElectionsNumber() != community.getElections().getElectionsNumber())
            throw new BadRequestException("A candidate doesn't take part in elections");
        if (voteRepository.existsByElectionsParticipationAndVoter(electionsParticipation, voter))
            throw new BadRequestException("User has already voted in these elections");
        if (getElectionsStatus(community.getElections()) != ElectionsStatus.IN_PROGRESS)
            throw new BadRequestException("Elections haven't started yet");


        var vote = Vote.builder()
                .electionsParticipation(electionsParticipation)
                .build();
        voteRepository.save(vote);
    }

    private Candidate getCandidateAfterCheck(BiConsumer<Community, UserEntity> checkFunction, String groupname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var user = userHelper.getCurrentUser();
        throwErrIfNotCitizenRight(community, user);
        checkFunction.accept(community, user);
        return findCandidateOrThrowErr(community, user);
    }

    public void activateCandidate(String groupname) {
        var candidate = getCandidateAfterCheck((c, u) -> {
            throwErrIfNotCitizenRight(c, u);
            if (getElectionsStatus(c.getElections()) != ElectionsStatus.NOT_STARTED)
                throw new BadRequestException("Elections have been already started");
        }, groupname);

        candidateRepository.save(candidate);

        var electionsParticipation = candidate
                .getCommunity()
                .getElections()
                .createElectionsParticipation(candidate);
        electionsParticipationRepository.save(electionsParticipation);
    }

    public void updateCandidateProgram(NewCandidateDto dto) {
        var candidate = getCandidateAfterCheck(
                this::throwErrIfNotCitizenRight,
                dto.getGroupname()
        );
        candidate.setProgram(dto.getProgram());
        candidateRepository.save(candidate);
    }

    @Transactional
    public void becomeCandidate(NewCandidateDto dto) {
        var community = communityHelper.getCommunityByNameOrThrowErr(dto.getGroupname());
        var user = userHelper.getCurrentUser();
        throwErrIfNotDemocracy(community);
        throwErrIfNotCitizenRight(community, user);
        if (getElectionsStatus(community.getElections()) != ElectionsStatus.NOT_STARTED)
            throw new BadRequestException("Elections have been already started");

        var candidate = Candidate.builder()
                .program(dto.getProgram())
                .community(community)
                .build();
        candidateRepository.save(candidate);

        var electionsParticipation = community.getElections().createElectionsParticipation(candidate);
        electionsParticipationRepository.save(electionsParticipation);
    }

    @Transactional
    public CandidateListDto getListOfCandidates(String groupname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        throwErrIfNotDemocracy(community);
        var membership = membershipHelper.getOptionalMembershipOfCurrentUser(community);
        communityHelper.throwErrIfNoAccessToCommunity(community, membership);

        Long votedId = (long) -1;
        if (userHelper.isContextUser()) {
            var candidate = voteRepository.getCandidateByCommunityAndVoter(
                    community,
                    userHelper.getCurrentUser()
            );

            if (candidate.isPresent())
                votedId = candidate.get().getId();
        }

        var status = getElectionsStatus(community.getElections());
        var candidates = candidateRepository.getAllCurrentByElections(community.getElections());

        var dto = CandidateListDto.builder()
                .currentCandidates(
                        candidates
                                .map(candidate -> {
                                    var user = candidate.getUser();
                                    return CandidateDto.builder()
                                            .id(candidate.getId())
                                            .name(user.getName())
                                            .surname(user.getSurname())
                                            .image(userHelper.getTopUserImage(user))
                                            .nickname(user.getNickname())
                                            .program(candidate.getProgram())
                                            .build();
                                    })
                                .collect(Collectors.toList())
                )
                .totalVotes(status == ElectionsStatus.IN_PROGRESS
                        ? 0
                        : voteRepository.getTotalVotesOfPrevElections(community.getElections())
                )
                .votedId(votedId);

        if (status != ElectionsStatus.IN_PROGRESS) {
            var previousCandidates = electionsParticipationRepository
                    .getAllPreviousElectionsCandidates(community.getElections());

            dto.previousCandidates(
                    previousCandidates.stream()
                            .map(participation -> CandidateDto.builder()
                                    .id(participation.getCandidate().getId())
                                    .nickname(participation.getCandidate().getUser().getNickname())
                                    .votes(participation.getNumberOfVotes())
                                    .build()
                            )
                            .collect(Collectors.toList())
            );
        }

        return dto.build();
    }



}
