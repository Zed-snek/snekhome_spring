package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.CommunityRoleDto;
import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityCardDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityDto;
import ed.back_snekhome.dto.communityDTOs.UpdateCommunityDto;
import ed.back_snekhome.entities.community.*;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.entities.communityDemocracy.CommunityCitizenParameters;
import ed.back_snekhome.entities.communityDemocracy.ElectionsParticipation;
import ed.back_snekhome.entities.communityDemocracy.PresidencyData;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityAlreadyExistsException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.community.*;
import ed.back_snekhome.repositories.democracy.*;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.utils.MyFunctions;
import ed.back_snekhome.helperComponents.CommunityHelper;
import ed.back_snekhome.helperComponents.MembershipHelper;
import ed.back_snekhome.helperComponents.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final FileService fileService;
    private final UserHelper userHelper;
    private final CommunityHelper communityHelper;
    private final MembershipHelper membershipHelper;
    private final CommunityLogService communityLogService;
    private final DemocracyService democracyService;

    private final CommunityRepository communityRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final CitizenParametersRepository citizenParametersRepository;
    private final MembershipRepository membershipRepository;
    private final CommunityImageRepository communityImageRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final CandidateRepository candidateRepository;
    private final PresidencyDataRepository presidencyDataRepository;
    private final ElectionsParticipationRepository electionsParticipationRepository;


    @Transactional
    public void newCommunity(NewCommunityDto dto) {

        var community = Community.builder()
                .type(dto.getType())
                .groupname(dto.getIdName())
                .name(dto.getName())
                .description(dto.getDescription())
                .isInviteUsers(dto.isInviteUsers())
                .isClosed(dto.isClosed())
                .build();
        if (dto.getType() == CommunityType.NEWSPAPER)
            community.setAnonAllowed(false);
        else
            community.setAnonAllowed(dto.isAnonAllowed());
        communityRepository.save(community);

        var ownerRoleBuilder = CommunityRole.builder()
                .community(community)
                .bannerColor("#15151D")
                .textColor("#E3E3E3")
                .isCreator(true);
        if (!(dto.getType() == CommunityType.ANARCHY)) {
            ownerRoleBuilder
                    .title(dto.getType() == CommunityType.DEMOCRACY ? "president" : "owner")
                    .banCitizen(true)
                    .deletePosts(true)
                    .editDescription(true)
                    .banUser(true)
                    .editId(true)
                    .inviteUsers(true);
        }
        else {
            ownerRoleBuilder.title("creator");
        }

        var ownerRole = ownerRoleBuilder.build();
        communityRoleRepository.save(ownerRole);

        var membership = Membership.builder()
                .community(community)
                .role(ownerRole)
                .build();
        membershipRepository.save(membership);

        if (dto.getType() == CommunityType.DEMOCRACY)
            createStartDemocracyData(community, dto);
    }

    public void createStartDemocracyData(
            Community community,
            NewCommunityDto dto
    ) {
        var citizenRole = CommunityRole.builder()
                .title(dto.getTitle())
                .bannerColor(dto.getBannerColor())
                .textColor(dto.getTextColor())
                .isCitizen(true)
                .community(community)
                .build();
        communityRoleRepository.save(citizenRole);

        var citizenParameters = CommunityCitizenParameters.builder()
                .days(dto.getCitizenDays())
                .rating(dto.getCitizenRating())
                .electionDays(dto.getElectionDays())
                .community(community)
                .build();
        citizenParametersRepository.save(citizenParameters);

        var presidencyData = PresidencyData.builder()
                .community(community)
                .build();
        presidencyData.clearData();
        presidencyDataRepository.save(presidencyData);

        var currentPresident = Candidate.builder()
                .community(community)
                .program("First creator")
                .build();
        candidateRepository.save(currentPresident);

        var elections = democracyService.createElections(currentPresident, community);
        var electionsParticipation = ElectionsParticipation.builder()
                .candidate(currentPresident)
                .electionsNumber(0)
                .elections(elections)
                .build();
        electionsParticipationRepository.save(electionsParticipation);
    }

    public void deleteCommunity(String name) {
        var community = communityHelper.getCommunityByNameOrThrowErr(name);
        if (community.getOwner().equals(userHelper.getCurrentUser())
                && community.getType() != CommunityType.DEMOCRACY)
            communityRepository.delete(community);
        else
            throw new UnauthorizedException("User doesn't have permissions to delete the community");
    }

    public boolean isNameTaken(String name) {
        userHelper.throwErrIfExistsByNickname(name);
        return true;
    }


    public PublicCommunityDto getPublicCommunityDto(String name) {
        var community = communityHelper.getCommunityByNameOrThrowErr(name);
        var membership = membershipHelper.getOptionalMembershipOfCurrentUser(community);

        PublicCommunityDto dto;
        if (communityHelper.isAccessToCommunity(community, membership)) {
            dto = PublicCommunityDto.builder()
                    .community(community)
                    .members(communityHelper.countMembers(community))
                    .ownerNickname(community.getOwner().getNickname())
                    .ownerImage(userHelper.getTopUserImage(community.getOwner()))
                    .isAccess(true)
                    .build();

            if (community.isClosed())
                dto.setJoinRequests(joinRequestRepository.countAllByCommunity(community));
        }
        else { //Limited information, if user has no permissions:
            dto = PublicCommunityDto.builder()
                    .name(community.getName())
                    .groupname(community.getGroupname())
                    .description(community.getDescription())
                    .image(communityHelper.getTopCommunityImage(community))
                    .type(community.getType())
                    .isAccess(false)
                    .isRequestSent(joinRequestRepository
                            .existsByCommunityAndUser(community, userHelper.getCurrentUser())
                    )
                    .build();
        }

        if (membership.isPresent()) {
            if (membership.get().isBanned()) {
                dto.setBanned(true);
            }
            else {
                dto.setMember(true);
                dto.setCurrentUserRole(membership.get().getRole());
            }
        }
        return dto;
    }

    public List<PublicCommunityCardDto> getHomeCards() {
        var list =
                membershipRepository.findTop4ByUserAndIsBanned(userHelper.getCurrentUser(), false);

        return list.stream().map(m -> PublicCommunityCardDto.builder()
                        .image(communityHelper.getTopCommunityImage(m.getCommunity()))
                        .groupname(m.getCommunity().getGroupname())
                        .build())
                .collect(Collectors.toList());
    }

    public void updateCommunity(UpdateCommunityDto dto) {
        var user = userHelper.getCurrentUser();
        var community = communityHelper.getCommunityByNameOrThrowErr(dto.getOldGroupname());
        var membership = membershipRepository.findByCommunityAndUser(community, user);
        if (membership.isPresent() && membership.get().getRole() != null) {
            var role = membership.get().getRole();

            if (dto.getGroupname() != null) {
                if (role.isEditId()) {
                    isNameTaken(dto.getGroupname());
                    community.setGroupname(dto.getGroupname());
                    communityLogService.createLogNewGroupname(community, dto.getGroupname());
                }
                else
                    throw new UnauthorizedException("User doesn't have permissions");
            }
            else if (role.isEditDescription()) {
                if (dto.getDescription() != null) {
                    community.setDescription(dto.getDescription());
                    communityLogService.createLogNewDescription(community, dto.getDescription());
                }
                else if (dto.getName() != null) {
                    community.setName(dto.getName());
                    communityLogService.createLogNewCommunityTitle(community, dto.getName());
                }

            }
            else
                throw new UnauthorizedException("User doesn't have permissions");
            communityRepository.save(community);
        }
    }

    public OwnSuccessResponse uploadCommunityImage(MultipartFile file, String groupname) throws IOException {

        String newName = fileService.uploadImageNameReturned(file);
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var image = CommunityImage.builder()
                .name(newName)
                .community(community)
                .build();
        communityImageRepository.save(image);
        communityLogService.createLogUpdateImage(community, false);
        return new OwnSuccessResponse(newName); //returns new name of uploaded file
    }

    public void newRole(CommunityRoleDto dto, String groupname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        if (communityHelper.isCurrentUserOwner(community) || community.getType() == CommunityType.ANARCHY) {
            if (communityRoleRepository.existsByCommunityAndTitle(community, dto.getTitle())) {
                throw new EntityAlreadyExistsException("Role with entered name is already exists");
            }
            else {
                var role = CommunityRole.builder()
                        .community(community)
                        .title(dto.getTitle())
                        .bannerColor(dto.getBannerColor())
                        .textColor(dto.getTextColor())
                        .deletePosts(dto.isDeletePosts())
                        .banUser(dto.isBanUser())
                        .banCitizen(dto.isBanCitizen())
                        .editId(dto.isEditId())
                        .editDescription(dto.isEditDescription())
                        .build();
                communityRoleRepository.save(role);
            }
        }
        else {
            throw new UnauthorizedException("User doesn't have permissions to create new roles");
        }
    }

    public void updateRole(CommunityRoleDto dto, String groupname, String oldRoleName) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        if (communityHelper.isCurrentUserOwner(community)) {
            if (!dto.getTitle().equals(oldRoleName)
                    && communityRoleRepository.existsByCommunityAndTitle(community, dto.getTitle())
            ) {
                throw new EntityAlreadyExistsException("Role with entered name is already exists");
            }
            else {
                var role = communityHelper.findRoleOrThrowErr(community, oldRoleName);

                MyFunctions.setIfNotEquals(role.getTitle(), dto.getTitle(), role::setTitle);
                MyFunctions.setIfNotEquals(role.getTextColor(), dto.getTextColor(), role::setTextColor);
                MyFunctions.setIfNotEquals(role.getBannerColor(), dto.getBannerColor(), role::setBannerColor);

                if (!(role.isCitizen() || role.isCreator())){
                    MyFunctions.setIfNotEquals(role.isBanUser(), dto.isBanUser(), role::setBanUser);
                    MyFunctions.setIfNotEquals(role.isBanCitizen(), dto.isBanCitizen(), role::setBanCitizen);
                    MyFunctions.setIfNotEquals(role.isDeletePosts(), dto.isDeletePosts(), role::setDeletePosts);
                    MyFunctions.setIfNotEquals(role.isEditDescription(), dto.isEditDescription(), role::setEditDescription);
                    MyFunctions.setIfNotEquals(role.isEditId(), dto.isEditId(), role::setEditId);
                    MyFunctions.setIfNotEquals(role.isInviteUsers(), dto.isInviteUsers(), role::setInviteUsers);
                }

                communityRoleRepository.save(role);
            }
        }
    }

    public List<CommunityRole> getRoles(String groupname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        return communityRoleRepository.findAllByCommunity(community);
    }

    @Transactional
    public void deleteRole(String groupname, String roleName) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var role = communityHelper.findRoleOrThrowErr(community, roleName);
        if (role.isCreator() || role.isCitizen())
            throw new BadRequestException("Action is not able due to citizen policy");

        if (communityHelper.isCurrentUserOwner(community)) {
            var memberships = membershipRepository.findAllByCommunityAndRole(community, role);
            memberships.forEach(m -> {
                m.setRole(null);
                membershipRepository.save(m);
            });
            communityRoleRepository.delete(role);
        }
        else {
            throw new UnauthorizedException("No permission to delete role");
        }
    }

    private <V> void setIfNotEqualsWithLog(V value, V value2, Consumer<V> setter,
                                           Community community, BiConsumer<Community, V> logger
    ) {
        if (!Objects.equals(value, value2)) {
            setter.accept(value2);
            logger.accept(community, value2);
        }
    }

    public void updateCommunitySettings(String groupname, NewCommunityDto dto) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        if (communityHelper.isCurrentUserOwner(community)) {

            setIfNotEqualsWithLog(community.isClosed(), dto.isClosed(), community::setClosed,
                    community, communityLogService::createLogRuleClosedCommunity);

            setIfNotEqualsWithLog(community.isAnonAllowed(), dto.isAnonAllowed(), community::setAnonAllowed,
                community, communityLogService::createLogRuleAnonPosts);

            setIfNotEqualsWithLog(community.isInviteUsers(), dto.isInviteUsers(), community::setInviteUsers,
                community, communityLogService::createLogRuleInviteUsers);

            communityRepository.save(community);
        }
    }

    public void updateCommunityDemocracySettings(String groupname, NewCommunityDto dto) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        if (communityHelper.isCurrentUserOwner(community)) {

            var parameters = community.getCitizenParameters();

            setIfNotEqualsWithLog(parameters.getDays(), dto.getCitizenDays(), parameters::setDays,
                    community, communityLogService::createLogNewCitizenRequirementsDays);

            setIfNotEqualsWithLog(parameters.getElectionDays(), dto.getElectionDays(), parameters::setElectionDays,
                    community, communityLogService::createLogNewElectionsPeriod);

            setIfNotEqualsWithLog(parameters.getRating(), dto.getCitizenRating(), parameters::setRating,
                    community, communityLogService::createLogNewCitizenRequirementsRating);

            citizenParametersRepository.save(parameters);
        }
    }

}
