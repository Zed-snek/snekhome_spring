package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.CommunityRoleDto;
import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityCardDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityDto;
import ed.back_snekhome.dto.communityDTOs.UpdateCommunityDto;
import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.community.*;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityAlreadyExistsException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.*;
import ed.back_snekhome.response.OwnSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final FileService fileService;
    private final UserMethodsService userMethodsService;
    private final CommunityMethodsService communityMethodsService;
    private final RelationsService relationsService;

    private final CommunityRepository communityRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final CitizenParametersRepository citizenParametersRepository;
    private final MembershipRepository membershipRepository;
    private final CommunityImageRepository communityImageRepository;
    private final JoinRequestRepository joinRequestRepository;



    @Transactional
    public void newCommunity(NewCommunityDto dto) {
        var owner = userMethodsService.getCurrentUser();

        var community = Community.builder()
                .type( dto.getType() )
                .groupname( dto.getIdName() )
                .name( dto.getName() )
                .description( dto.getDescription() )
                .isClosed( dto.isClosed() )
                .isInviteUsers( dto.isInviteUsers() )
                .owner( owner )
                .creation( LocalDate.now() )
                .build();

        if (dto.getType() == CommunityType.NEWSPAPER)
            community.setAnonAllowed(false);
        else
            community.setAnonAllowed(dto.isAnonAllowed());

        communityRepository.save(community);

        var membership = Membership.builder()
                .user(owner)
                .community(community)
                .build();

        CommunityRole ownerRole;
        if (!(dto.getType() == CommunityType.ANARCHY)) {
            String title;
            boolean isCitizen;
            if (dto.getType() == CommunityType.DEMOCRACY) {
                title = "president";
                isCitizen = true;
            }
            else {
                title = "owner";
                isCitizen = false;
            }

            ownerRole = CommunityRole.builder()
                    .title(title)
                    .community(community)
                    .bannerColor("#15151D")
                    .textColor("#E3E3E3")
                    .banCitizen(true)
                    .deletePosts(true)
                    .editDescription(true)
                    .banUser(true)
                    .editId(true)
                    .isCitizen(isCitizen)
                    .isCreator(true)
                    .inviteUsers(true)
                    .build();
        }
        else {
            ownerRole = CommunityRole.builder()
                    .title("creator")
                    .community(community)
                    .bannerColor("#15151D")
                    .textColor("#E3E3E3")
                    .banCitizen(false)
                    .deletePosts(false)
                    .editDescription(false)
                    .banUser(false)
                    .editId(false)
                    .isCitizen(false)
                    .isCreator(true)
                    .inviteUsers(false)
                    .build();
        }
        communityRoleRepository.save(ownerRole);
        membership.setRole(ownerRole);
        membershipRepository.save(membership);

        if (dto.getType() == CommunityType.DEMOCRACY) {
            var citizenRole = CommunityRole.builder()
                    .title( dto.getTitle() )
                    .bannerColor( dto.getBannerColor() )
                    .textColor( dto.getTextColor() )
                    .isCitizen( true )
                    .community( community )
                    .build();
            communityRoleRepository.save(citizenRole);

            var citizenParameters = CommunityCitizenParameters.builder()
                    .days( dto.getCitizenDays() )
                    .rating( dto.getCitizenRating() )
                    .electionDays( dto.getElectionDays() )
                    .community( community )
                    .build();
            citizenParametersRepository.save(citizenParameters);
        }
    }

    public void deleteCommunity(String name) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(name);
        if (community.getOwner().equals(userMethodsService.getCurrentUser())
                && community.getType() != CommunityType.DEMOCRACY
        )
            communityRepository.delete(community);
        else
            throw new UnauthorizedException("User doesn't have permissions to delete the community");
    }

    public boolean isNameTaken(String name) {
        userMethodsService.throwErrIfExistsByNickname(name);
        return true;
    }


    public PublicCommunityDto getPublicCommunityDto(String name) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(name);

        var membership = communityMethodsService.getOptionalMembershipOfCurrentUser(community);

        if (communityMethodsService.isAccessToCommunity(community, membership)) {
            var dto = PublicCommunityDto.builder()
                    .community(community)
                    .members(communityMethodsService.countMembers(community))
                    .ownerNickname(community.getOwner().getNickname())
                    .ownerImage(userMethodsService.getTopUserImage(community.getOwner()))
                    .isAccess(true)
                    .build();
            dto.setMember(false);

            if (membership.isPresent()) {
                if (membership.get().isBanned()) {
                    dto.setBanned(true);
                }
                else {
                    dto.setMember(true);
                    dto.setCurrentUserRole(membership.get().getRole());
                }
            }
            if (community.isClosed())
                dto.setJoinRequests(joinRequestRepository.countAllByCommunity(community));

            return dto;
        }
        else { //Limited information, if user has no permissions:
            return PublicCommunityDto.builder()
                    .name(community.getName())
                    .groupname(community.getGroupname())
                    .description(community.getDescription())
                    .image(communityMethodsService.getTopCommunityImage(community))
                    .type(community.getType())
                    .isAccess(false)
                    .isRequestSent(joinRequestRepository.existsByCommunityAndUser(
                            community,
                            userMethodsService.getCurrentUser())
                    )
                    .build();
        }
    }

    public ArrayList<PublicCommunityCardDto> getHomeCards() {
        var list =
                membershipRepository.findTop4ByUserAndIsBanned(userMethodsService.getCurrentUser(), false);
        var array = new ArrayList<PublicCommunityCardDto>();
        list.forEach(o ->
                array.add(PublicCommunityCardDto.builder()
                        .image(communityMethodsService.getTopCommunityImage(o.getCommunity()))
                        .groupname( o.getCommunity().getGroupname() )
                        .build()));
        return array;
    }

    public void updateCommunity(UpdateCommunityDto dto) {
        var user = userMethodsService.getCurrentUser();
        var community = communityMethodsService.getCommunityByNameOrThrowErr(dto.getOldGroupname());
        var membership = membershipRepository.findByCommunityAndUser(community, user);
        if (membership.isPresent() && membership.get().getRole() != null) {
            var role = membership.get().getRole();

            if (dto.getGroupname() != null) {
                if (role.isEditId()) {
                    isNameTaken(dto.getGroupname());
                    community.setGroupname(dto.getGroupname());
                }
                else
                    throw new UnauthorizedException("User doesn't have permissions");
            }
            else if (role.isEditDescription()) {
                if (dto.getDescription() != null)
                    community.setDescription(dto.getDescription());
                else if (dto.getName() != null)
                    community.setName(dto.getName());
            }
            else
                throw new UnauthorizedException("User doesn't have permissions");
            communityRepository.save(community);
        }
    }

    public OwnSuccessResponse uploadCommunityImage(MultipartFile file, String groupname) throws IOException {

        String newName = fileService.uploadImageNameReturned(file);
        var image = CommunityImage.builder()
                .name(newName)
                .community(communityMethodsService.getCommunityByNameOrThrowErr(groupname))
                .build();
        communityImageRepository.save(image);
        return new OwnSuccessResponse(newName); //returns new name of uploaded file
    }

    public void newRole(CommunityRoleDto dto, String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (communityMethodsService.isCurrentUserOwner(community) || community.getType() == CommunityType.ANARCHY) {
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
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (communityMethodsService.isCurrentUserOwner(community)) {
            if (!dto.getTitle().equals(oldRoleName)
                    && communityRoleRepository.existsByCommunityAndTitle(community, dto.getTitle())
            ) {
                throw new EntityAlreadyExistsException("Role with entered name is already exists");
            }
            else {
                var role = communityMethodsService.findRoleOrThrowErr(community, oldRoleName);
                role.setTitle(dto.getTitle());
                role.setTextColor(dto.getTextColor());
                role.setBannerColor(dto.getBannerColor());
                role.setBanUser(dto.isBanUser());
                role.setBanCitizen(dto.isBanCitizen());
                role.setDeletePosts(dto.isDeletePosts());
                role.setEditDescription(dto.isEditDescription());
                role.setEditId(dto.isBanUser());
                communityRoleRepository.save(role);
            }
        }
    }

    public Iterable<CommunityRole> getRoles(String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        return communityRoleRepository.findAllByCommunity(community);
    }

    @Transactional
    public void deleteRole(String groupname, String roleName) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (communityMethodsService.isCurrentUserOwner(community)) {
            var role = communityMethodsService.findRoleOrThrowErr(community, roleName);
            var memberships = membershipRepository.findAllByCommunityAndRole(community, role);
            for (var m: memberships) {
                m.setRole(null);
                membershipRepository.save(m);
            }
            communityRoleRepository.delete(role);
        }
    }

    public void updateCommunitySettings(String groupname, NewCommunityDto dto) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (communityMethodsService.isCurrentUserOwner(community)) {
            community.setClosed(dto.isClosed());
            community.setAnonAllowed(dto.isAnonAllowed());
            community.setInviteUsers(dto.isInviteUsers());
            communityRepository.save(community);
        }
    }

    public void updateCommunityDemocracySettings(String groupname, NewCommunityDto dto) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (communityMethodsService.isCurrentUserOwner(community)) {
            var parameters = citizenParametersRepository.findTopByCommunity(community)
                    .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
            parameters.setDays(dto.getCitizenDays());
            parameters.setElectionDays(dto.getElectionDays());
            parameters.setRating(dto.getCitizenRating());
            citizenParametersRepository.save(parameters);
        }
    }

    public String manageJoinRequest(String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var user = userMethodsService.getCurrentUser();

        var membership = membershipRepository.findByCommunityAndUser(community, user);
        if (!community.isClosed() || membership.isPresent())
            throw new BadRequestException("Bad request");

        var request = joinRequestRepository.findTopByCommunityAndUser(community, user);
        if (request.isPresent()) {
            joinRequestRepository.delete(request.get());
            return "Request is cancelled successfully";
        }

        var newRequest = JoinRequest.builder()
                .community(community)
                .user(user)
                .build();
        joinRequestRepository.save(newRequest);
        return "Request is sent successfully";
    }

    public ArrayList<UserPublicDto> getAllJoinRequests(String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var user = userMethodsService.getCurrentUser();

        var membership = relationsService.getMembershipOrThrowErr(user, community);
        var array = new ArrayList<UserPublicDto>();
        if (membership.getRole().isInviteUsers() || community.getType() == CommunityType.ANARCHY) {
            Iterable<JoinRequest> list = joinRequestRepository.findAllByCommunity(community);
            for (JoinRequest r : list) {
                array.add(UserPublicDto.builder()
                                .nickname(r.getUser().getNickname())
                                .image(userMethodsService.getTopUserImage(r.getUser()))
                        .build()
                );
            }
            return array;
        }

        throw new UnauthorizedException("No access to data");
    }

    private void deleteJoinRequest(Community community, UserEntity user) {
        var request = joinRequestRepository.findTopByCommunityAndUser(community, user);
        if (request.isPresent())
            joinRequestRepository.delete(request.get());
        else
            throw new EntityNotFoundException("There is no request by user @" + user.getNickname());
    }

    @Transactional
    public void acceptJoinRequest(String groupname, String nickname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var user = userMethodsService.getUserByNickname(nickname);
        deleteJoinRequest(community, user);

        var membership = Membership.builder()
                .user(user)
                .community(community)
                .build();
        membershipRepository.save(membership);
    }

    public void cancelJoinRequest(String groupname, String nickname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var user = userMethodsService.getUserByNickname(nickname);
        deleteJoinRequest(community, user);
    }


}
