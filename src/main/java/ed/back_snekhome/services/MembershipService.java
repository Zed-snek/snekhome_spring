package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.MembersDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityCardDto;
import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.community.JoinRequest;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.entities.relations.Friendship;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.CommunityRoleRepository;
import ed.back_snekhome.repositories.FriendshipRepository;
import ed.back_snekhome.repositories.JoinRequestRepository;
import ed.back_snekhome.repositories.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserMethodsService userMethodsService;
    private final CommunityMethodsService communityMethodsService;
    private final CommunityLogService communityLogService;

    private final MembershipRepository membershipRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final JoinRequestRepository joinRequestRepository;


    public Membership getMembershipOrThrowErr(UserEntity user, Community community) {
        return membershipRepository.findByCommunityAndUser(community, user)
                .orElseThrow(() -> new EntityNotFoundException("User is not a member"));
    }
    public Membership getMembershipOfCurrentUserOrThrowErr(Community community) {
        return getMembershipOrThrowErr(userMethodsService.getCurrentUser(), community);
    }

    public Iterable<Membership> getMembershipsByUser(UserEntity user, boolean isBanned) {
        return membershipRepository.findAllByUserAndIsBanned(user, isBanned);
    }

    public Iterable<Membership> getMembershipsByCommunity(Community community, boolean isBanned) {
        return membershipRepository.findAllByCommunityAndIsBanned(community, isBanned);
    }

    public void joinCommunity(String groupname) {
        var current = userMethodsService.getCurrentUser();
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var optional = membershipRepository.findByCommunityAndUser(community, current);
        if (optional.isPresent() && optional.get().isBanned()) {
            throw new UnauthorizedException("You are banned");
        }
        var membership = Membership.builder()
                .user(current)
                .community(community)
                .build();
        if (community.getOwner().equals(current))
            membership.setRole(communityRoleRepository.findTopByCommunityAndIsCreator(community, true)
                    .orElse(null));
        membershipRepository.save(membership);
    }
    public void leaveCommunity(String groupname) {
        var membership = getMembershipOrThrowErr(
                userMethodsService.getCurrentUser(),
                communityMethodsService.getCommunityByNameOrThrowErr(groupname)
        );
        membershipRepository.delete(membership);
    }

    public ArrayList<PublicCommunityCardDto> getJoinedCommunitiesByNickname(String nickname) {
        var user = userMethodsService.getUserByNicknameOrThrowErr(nickname);
        var memberships = getMembershipsByUser(user, false);
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
        var memberships = getMembershipsByCommunity(community, isBanned);
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
        var userMembership = getMembershipOrThrowErr(userEntity, community);
        var admin = userMethodsService.getCurrentUser();
        var adminMembership = getMembershipOrThrowErr(admin, community);

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
                    = getMembershipOrThrowErr(user, community);
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
                    = getMembershipOrThrowErr(user, community);
            String roleTitle = membership.getRole().getTitle();
            membership.setRole(null);
            membershipRepository.save(membership);
            communityLogService.createLogRevokeRole(community, user, roleTitle);
        }
    }

    public Optional<Membership> getOptionalMembershipOfCurrentUser(Community community) {
        if (userMethodsService.isContextUser())
            return getOptionalMembershipOfUser(community, userMethodsService.getCurrentUser());
        return Optional.empty();
    }

    public Optional<Membership> getOptionalMembershipOfUser(Community community, UserEntity user) {
        return membershipRepository.findByCommunityAndUser(community, user);
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

        var membership = getMembershipOrThrowErr(user, community);
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
