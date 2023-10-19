package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.CandidateDto;
import ed.back_snekhome.dto.communityDTOs.GeneralDemocracyDto;
import ed.back_snekhome.dto.communityDTOs.NewCandidateDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.Elections;
import ed.back_snekhome.entities.communityDemocracy.PresidencyData;
import ed.back_snekhome.entities.communityDemocracy.Vote;
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
import ed.back_snekhome.repositories.democracy.PresidencyDataRepository;
import ed.back_snekhome.repositories.democracy.CandidateRepository;
import ed.back_snekhome.repositories.democracy.ElectionsRepository;
import ed.back_snekhome.repositories.democracy.VoteRepository;
import ed.back_snekhome.repositories.post.CommentaryRatingRepository;
import ed.back_snekhome.repositories.post.PostRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
            var optRole = optionalMembership.map(Membership::getRole);
            if (optRole.isPresent() && optRole.get().isCreator())
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
            var winner = candidateRepository.findCandidateWithMostVotes(community);
            winner.ifPresent(candidate -> { //(if 0 votes, leaves the same president)
                updateElectionsDate(elections, candidate);
                community.setOwner(candidate.getUser());
            });
            clearPresidencyDataByCommunity(community);
            candidateRepository.makeAllCandidatesInactive(community);

            communityRepository.save(community);
            return false;
        }

        boolean isElectionsNow = status == ElectionsStatus.IN_PROGRESS;
        if (isElectionsNow && !elections.isActive()) { //if elections just have started
            elections.setActive(true);
            electionsRepository.save(elections);
            voteRepository.deleteAllByCommunity(elections.getCommunity());
        }

        return isElectionsNow;
    }

    @Transactional
    public GeneralDemocracyDto getGeneralDemocracyData(String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        throwErrIfNotDemocracy(community);

        Optional<Membership> optMembership;
        UserEntity user = null;
        if (userMethodsService.isContextUser()) {
            user = userMethodsService.getCurrentUser();
            optMembership = membershipMethodsService.getOptionalMembershipOfUser(community, user);
        }
        else {
            optMembership = Optional.empty();
        }
        communityMethodsService.throwErrIfNoAccessToCommunity(community, optMembership);

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
                                    dtoBuilder.isCurrentUserActiveCandidate(candidate.isActive());
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
                .deletedPostsStats(presidencyData.getDeletedPosts())
                .citizenAmount(membershipRepository.countCitizensByCommunity(community));

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
        return elections;
    }

    public void createElections(Candidate currentPresident, Community community) {
        var elections = new Elections();
        elections.setCommunity(community);
        electionsRepository.save(updateElectionsDate(elections, currentPresident));
    }

    private Candidate findCandidateOrThrowErr(Community community, UserEntity user) {
        return candidateRepository.findTopByUserAndCommunity(user, community)
                .orElseThrow(() -> new EntityNotFoundException(user.getNickname() + " is not a candidate"));
    }

    public void makeVote(String groupname, String candidateNickname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var userCandidate = userMethodsService.getUserByNicknameOrThrowErr(candidateNickname);
        var candidate = findCandidateOrThrowErr(community, userCandidate);
        var voter = userMethodsService.getCurrentUser();

        throwErrIfNotCitizenRight(community, voter);
        if (voteRepository.existsByCandidateAndVoter(candidate, voter))
            throw new BadRequestException("User has already voted in these elections");
        if (getElectionsStatus(community.getElections()) != ElectionsStatus.IN_PROGRESS)
            throw new BadRequestException("Elections haven't started yet");

        var vote = Vote.builder()
                .candidate(candidate)
                .voter(voter)
                .build();
        voteRepository.save(vote);
    }

    private Candidate getCandidateAfterCheck(BiConsumer<Community, UserEntity> checkFunction, String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var user = userMethodsService.getCurrentUser();
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
        candidate.setActive(true);
        candidateRepository.save(candidate);
    }

    public void updateCandidateProgram(NewCandidateDto dto) {
        var candidate = getCandidateAfterCheck(
                this::throwErrIfNotCitizenRight,
                dto.getGroupname()
        );
        candidate.setProgram(dto.getProgram());
        candidateRepository.save(candidate);
    }

    public void becomeCandidate(NewCandidateDto dto) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(dto.getGroupname());
        var user = userMethodsService.getCurrentUser();
        throwErrIfNotDemocracy(community);
        throwErrIfNotCitizenRight(community, user);
        if (getElectionsStatus(community.getElections()) != ElectionsStatus.NOT_STARTED)
            throw new BadRequestException("Elections have been already started");

        var candidate = Candidate.builder()
                .program(dto.getProgram())
                .community(community)
                .user(user)
                .isActive(true)
                .build();
        candidateRepository.save(candidate);
    }

    public List<CandidateDto> getListOfCandidates(String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        throwErrIfNotDemocracy(community);

        var status = getElectionsStatus(community.getElections());

        var candidates = candidateRepository.getAllByCommunityAndIsActiveTrue(community);
        return candidates.stream()
                .map(candidate -> {
                    var user = candidate.getUser();
                    return CandidateDto.builder()
                            .name(user.getName())
                            .surname(user.getSurname())
                            .image(userMethodsService.getTopUserImage(user))
                            .nickname(user.getNickname())
                            .program(candidate.getProgram())
                            .votes(status == ElectionsStatus.IN_PROGRESS
                                    ? 0
                                    : voteRepository.countAllByCandidate(candidate)
                            )
                            .build();
                    })
                .collect(Collectors.toList());
    }



}
