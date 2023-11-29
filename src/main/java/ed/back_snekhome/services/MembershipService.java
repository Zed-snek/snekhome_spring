package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.MembersDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityCardDto;
import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.community.JoinRequest;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.enums.PresidencyDataType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.community.CommunityRoleRepository;
import ed.back_snekhome.repositories.community.JoinRequestRepository;
import ed.back_snekhome.repositories.community.MembershipRepository;
import ed.back_snekhome.helperComponents.CommunityHelper;
import ed.back_snekhome.helperComponents.MembershipHelper;
import ed.back_snekhome.helperComponents.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipHelper membershipHelper;
    private final UserHelper userHelper;
    private final CommunityHelper communityHelper;
    private final CommunityLogService communityLogService;
    private final DemocracyService democracyService;
    private final NotificationService notificationService;

    private final MembershipRepository membershipRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final JoinRequestRepository joinRequestRepository;

    public void joinCommunity(String groupname) {
        var current = userHelper.getCurrentUser();
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var optional = membershipRepository.findByCommunityAndUser(community, current);

        optional.ifPresent(m -> {
            if (m.isBanned())
                throw new UnauthorizedException("You are banned");
            else
                throw new BadRequestException("You are already a member");
        });

        var membership = Membership.builder()
                .community(community)
                .build();
        if (community.getOwner().equals(current))
            membership.setRole(communityRoleRepository.findCreatorRoleOfCommunity(community)
                    .orElse(null));
        membershipRepository.save(membership);
    }


    public void leaveCommunity(String groupname) {
        var membership = membershipHelper.getMembershipOrThrowErr(
                userHelper.getCurrentUser(),
                communityHelper.getCommunityByNameOrThrowErr(groupname)
        );
        membershipRepository.delete(membership);
    }


    public ArrayList<PublicCommunityCardDto> getJoinedCommunitiesByNickname(String nickname) {
        var user = userHelper.getUserByNicknameOrThrowErr(nickname);
        var memberships = membershipHelper.getMembershipsByUser(user, false);
        var array = new ArrayList<PublicCommunityCardDto>();
        memberships.forEach(
                m -> array.add(PublicCommunityCardDto.builder()
                        .image(communityHelper.getTopCommunityImage(m.getCommunity()))
                        .name( m.getCommunity().getName() )
                        .groupname( m.getCommunity().getGroupname() )
                        .description( m.getCommunity().getDescription() )
                        .members( communityHelper.countMembers(m.getCommunity()) )
                        .build())
        );
        return array;
    }


    public MembersDto getMembersByCommunity(String groupname, boolean isBanned) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        if (community.isClosed() && !communityHelper.isContextUserMember(community)) {
            return MembersDto.builder()
                    .isContextUserAccess(false)
                    .build();
        }
        var memberships = membershipHelper.getMembershipsByCommunity(community, isBanned);
        var users = new ArrayList<UserPublicDto>();
        for (Membership m : memberships) {
            var user = m.getUser();
            users.add(UserPublicDto.builder()
                            .name(user.getName())
                            .surname(user.getSurname())
                            .nickname(user.getNickname())
                            .image(userHelper.getTopUserImage(user))
                            .communityRole(m.getRole())
                            .build());
        }
        List<String> roles = null;
        if (!isBanned) {
            roles = new ArrayList<>();
            for (CommunityRole r : communityRoleRepository.findAllByCommunity(community)) {
                if (!r.isCreator())
                    roles.add(r.getTitle());
            }
        }
        return MembersDto.builder()
                .users(users)
                .roles(roles)
                .isContextUserAccess(true)
                .build();
    }


    private Membership canBanUser(Community community, UserEntity userEntity) {
        var userMembership = membershipHelper.getMembershipOrThrowErr(userEntity, community);
        var admin = userHelper.getCurrentUser();
        var adminMembership = membershipHelper.getMembershipOrThrowErr(admin, community);

        if ((userMembership.getRole() == null && adminMembership.getRole().isBanUser())
                || (userMembership.getRole().isCitizen() && adminMembership.getRole().isBanCitizen())
                || adminMembership.getRole().isCreator()
        ) {
            return userMembership;
        }
        throw new UnauthorizedException("No permissions to ban user");
    }


    @Transactional
    public void banUser(String groupname, String nickname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var userEntity = userHelper.getUserByNicknameOrThrowErr(nickname);
        var userMembership = canBanUser(community, userEntity);
        userMembership.setBanned(true);

        if (democracyService.isCitizenRight(community, userEntity))
            democracyService.addStatsToPresidency(community, PresidencyDataType.BANNED_CITIZEN);
        else
            democracyService.addStatsToPresidency(community, PresidencyDataType.BANNED_USER);

        userMembership.setRole(null);
        membershipRepository.save(userMembership);

        communityLogService.createLogBanUser(community, userEntity);
        notificationService.createBannedInCommunityNotification(userEntity, community);
    }

    @Transactional
    public void unbanUser(String groupname, String user) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var userEntity = userHelper.getUserByNicknameOrThrowErr(user);
        var userMembership = canBanUser(community, userEntity);
        communityLogService.createLogUnbanUser(community, userEntity);
        membershipRepository.delete(userMembership);
    }


    @Transactional
    public void grantRole(String nickname, String groupname, String roleName) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        if (communityHelper.isCurrentUserOwner(community)) {
            var role = communityHelper.findRoleOrThrowErr(community, roleName);
            var user = userHelper.getUserByNicknameOrThrowErr(nickname);
            var membership
                    = membershipHelper.getMembershipOrThrowErr(user, community);
            membership.setRole(role);
            membershipRepository.save(membership);
            communityLogService.createLogGrantRole(community, user, roleName);
        }
    }


    @Transactional
    public void revokeRole(String nickname, String groupname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        if (communityHelper.isCurrentUserOwner(community)) {
            var user = userHelper.getUserByNicknameOrThrowErr(nickname);
            var membership
                    = membershipHelper.getMembershipOrThrowErr(user, community);
            String roleTitle = membership.getRole().getTitle();
            membership.setRole(null);
            membershipRepository.save(membership);
            communityLogService.createLogRevokeRole(community, user, roleTitle);
        }
    }


    public String manageJoinRequest(String groupname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var user = userHelper.getCurrentUser();

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
                .build();
        joinRequestRepository.save(newRequest);
        return "Request is sent successfully";
    }


    public List<UserPublicDto> getAllJoinRequests(String groupname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var user = userHelper.getCurrentUser();

        var membership = membershipHelper.getMembershipOrThrowErr(user, community);
        var array = new ArrayList<UserPublicDto>();
        if (membership.getRole().isInviteUsers() || community.getType() == CommunityType.ANARCHY) {
            Iterable<JoinRequest> list = joinRequestRepository.findAllByCommunity(community);
            for (JoinRequest r : list) {
                array.add(UserPublicDto.builder()
                        .nickname(r.getUser().getNickname())
                        .image(userHelper.getTopUserImage(r.getUser()))
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
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var user = userHelper.getUserByNicknameOrThrowErr(nickname);
        deleteJoinRequest(community, user);

        var membership = Membership.builder()
                .user(user)
                .community(community)
                .build();
        membershipRepository.save(membership);
        communityLogService.createLogAcceptJoinRequest(community, user);
    }


    public void cancelJoinRequest(String groupname, String nickname) {
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        var user = userHelper.getUserByNicknameOrThrowErr(nickname);
        deleteJoinRequest(community, user);
    }

}
