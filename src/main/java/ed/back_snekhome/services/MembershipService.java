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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipMethodsService membershipMethodsService;
    private final UserMethodsService userMethodsService;
    private final CommunityMethodsService communityMethodsService;
    private final CommunityLogService communityLogService;
    private final DemocracyService democracyService;

    private final MembershipRepository membershipRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final JoinRequestRepository joinRequestRepository;

    public void joinCommunity(String groupname) {
        var current = userMethodsService.getCurrentUser();
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var optional = membershipRepository.findByCommunityAndUser(community, current);

        optional.ifPresent(m -> {
            if (m.isBanned())
                throw new UnauthorizedException("You are banned");
            else
                throw new BadRequestException("You are already a member");
        });

        var membership = Membership.builder()
                .user(current)
                .community(community)
                .joined(LocalDate.now())
                .build();
        if (community.getOwner().equals(current))
            membership.setRole(communityRoleRepository.findTopByCommunityAndIsCreator(community, true)
                    .orElse(null));
        membershipRepository.save(membership);
    }

    public void leaveCommunity(String groupname) {
        var membership = membershipMethodsService.getMembershipOrThrowErr(
                userMethodsService.getCurrentUser(),
                communityMethodsService.getCommunityByNameOrThrowErr(groupname)
        );
        membershipRepository.delete(membership);
    }

    public ArrayList<PublicCommunityCardDto> getJoinedCommunitiesByNickname(String nickname) {
        var user = userMethodsService.getUserByNicknameOrThrowErr(nickname);
        var memberships = membershipMethodsService.getMembershipsByUser(user, false);
        var array = new ArrayList<PublicCommunityCardDto>();
        memberships.forEach(
                m -> array.add(PublicCommunityCardDto.builder()
                        .image(communityMethodsService.getTopCommunityImage(m.getCommunity()))
                        .name( m.getCommunity().getName() )
                        .groupname( m.getCommunity().getGroupname() )
                        .description( m.getCommunity().getDescription() )
                        .members( communityMethodsService.countMembers(m.getCommunity()) )
                        .build())
        );
        return array;
    }

    public MembersDto getMembersByCommunity(String groupname, boolean isBanned) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (community.isClosed() && !communityMethodsService.isContextUserMember(community)) {
            return MembersDto.builder()
                    .isContextUserAccess(false)
                    .build();
        }
        var memberships = membershipMethodsService.getMembershipsByCommunity(community, isBanned);
        var users = new ArrayList<UserPublicDto>();
        for (Membership m : memberships) {
            var user = m.getUser();
            users.add(UserPublicDto.builder()
                            .name(user.getName())
                            .surname(user.getSurname())
                            .nickname(user.getNickname())
                            .image(userMethodsService.getTopUserImage(user))
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
        var userMembership = membershipMethodsService.getMembershipOrThrowErr(userEntity, community);
        var admin = userMethodsService.getCurrentUser();
        var adminMembership = membershipMethodsService.getMembershipOrThrowErr(admin, community);

        if ((userMembership.getRole() == null && adminMembership.getRole().isBanUser())
                || (userMembership.getRole().isCitizen() && adminMembership.getRole().isBanCitizen())
                || adminMembership.getRole().isCreator()
        ) {
            return userMembership;
        }
        throw new UnauthorizedException("No permissions to ban user");
    }

    @Transactional
    public void banUser(String groupname, String user) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var userEntity = userMethodsService.getUserByNicknameOrThrowErr(user);
        var userMembership = canBanUser(community, userEntity);
        userMembership.setBanned(true);

        if (democracyService.isCitizenRight(community, userEntity))
            democracyService.addStatsToPresidency(community, PresidencyDataType.BANNED_CITIZEN);
        else
            democracyService.addStatsToPresidency(community, PresidencyDataType.BANNED_USER);

        userMembership.setRole(null);
        membershipRepository.save(userMembership);
        communityLogService.createLogBanUser(community, userEntity);

    }

    @Transactional
    public void unbanUser(String groupname, String user) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var userEntity = userMethodsService.getUserByNicknameOrThrowErr(user);
        var userMembership = canBanUser(community, userEntity);
        communityLogService.createLogUnbanUser(community, userEntity);
        membershipRepository.delete(userMembership);
    }

    @Transactional
    public void grantRole(String nickname, String groupname, String roleName) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (communityMethodsService.isCurrentUserOwner(community)) {
            var role = communityMethodsService.findRoleOrThrowErr(community, roleName);
            var user = userMethodsService.getUserByNicknameOrThrowErr(nickname);
            var membership
                    = membershipMethodsService.getMembershipOrThrowErr(user, community);
            membership.setRole(role);
            membershipRepository.save(membership);
            communityLogService.createLogGrantRole(community, user, roleName);
        }
    }

    @Transactional
    public void revokeRole(String nickname, String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (communityMethodsService.isCurrentUserOwner(community)) {
            var user = userMethodsService.getUserByNicknameOrThrowErr(nickname);
            var membership
                    = membershipMethodsService.getMembershipOrThrowErr(user, community);
            String roleTitle = membership.getRole().getTitle();
            membership.setRole(null);
            membershipRepository.save(membership);
            communityLogService.createLogRevokeRole(community, user, roleTitle);
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

    public List<UserPublicDto> getAllJoinRequests(String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var user = userMethodsService.getCurrentUser();

        var membership = membershipMethodsService.getMembershipOrThrowErr(user, community);
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
        var user = userMethodsService.getUserByNicknameOrThrowErr(nickname);
        deleteJoinRequest(community, user);

        var membership = Membership.builder()
                .user(user)
                .community(community)
                .build();
        membershipRepository.save(membership);
        communityLogService.createLogAcceptJoinRequest(community, user);
    }

    public void cancelJoinRequest(String groupname, String nickname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var user = userMethodsService.getUserByNicknameOrThrowErr(nickname);
        deleteJoinRequest(community, user);
    }

}
