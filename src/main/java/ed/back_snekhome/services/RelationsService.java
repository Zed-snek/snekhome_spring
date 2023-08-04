package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.MembersDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityCardDto;
import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.entities.relations.Friendship;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.CommunityRoleRepository;
import ed.back_snekhome.repositories.FriendshipRepository;
import ed.back_snekhome.repositories.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RelationsService {

    private final UserMethodsService userMethodsService;
    private final CommunityMethodsService communityMethodsService;
    private final FriendshipRepository friendshipRepository;
    private final MembershipRepository membershipRepository;
    private final CommunityRoleRepository communityRoleRepository;


    public void addFriend(String nickname) {
        manageFriend(nickname, true);
    }
    public void delFriend(String nickname) {
        manageFriend(nickname, false);
    }
    private void manageFriend(String nickname, boolean isAdd) {

        var requestUser = userMethodsService.getCurrentUser();
        var secondUser = userMethodsService.getUserByNickname(nickname);
        var friendship = getFriendshipOrCreate(requestUser.getIdAccount(), secondUser.getIdAccount());

        if (Objects.equals(friendship.getIdFirstUser(), requestUser.getIdAccount()))
            friendship.setFirstUser(isAdd);
        else
            friendship.setSecondUser(isAdd);
        friendshipRepository.save(friendship);
    }

    public Friendship getFriendshipOrCreate(Long firstUser, Long secondUser) {
        var friendship = friendshipRepository.findFriendshipByIdFirstUserAndIdSecondUser(firstUser, secondUser);
        if (friendship.isEmpty())
            friendship = friendshipRepository.findFriendshipByIdFirstUserAndIdSecondUser(secondUser, firstUser);

        return friendship.orElseGet(() -> Friendship.builder()
                .isFirstUser(false)
                .isSecondUser(false)
                .idFirstUser(firstUser)
                .idSecondUser(secondUser)
                .build());
    }

    private Iterable<Friendship> getFriendshipsByUserId(Long id) {
        return friendshipRepository.findAllByIdFirstUserOrIdSecondUser(id, id);
    }

    public ArrayList<UserPublicDto> getFriends(String nickname) {
        var user = userMethodsService.getUserByNickname(nickname);
        var friendships = getFriendshipsByUserId(user.getIdAccount());
        var array = new ArrayList<UserPublicDto>();
        for (Friendship f : friendships) {
            UserEntity friend;
            if (f.getIdFirstUser().equals(user.getIdAccount()))
                friend = userMethodsService.getUserById(f.getIdSecondUser());
            else
                friend = userMethodsService.getUserById(f.getIdFirstUser());
            array.add(UserPublicDto.builder()
                            .image(userMethodsService.getTopUserImage(friend))
                            .nickname(friend.getNickname())
                            .friendshipType(userMethodsService.getFriendshipType(user.getIdAccount(), friend.getIdAccount()))
                            .name(friend.getName())
                            .surname(friend.getSurname())
                    .build());
        }
        return array;
    }



    public Membership getMembershipOrThrowErr(UserEntity user, Community community) {
        return membershipRepository.findByCommunityAndUser(community, user)
                .orElseThrow(() -> new EntityNotFoundException("User is not a member"));
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
            membership.setRole(communityRoleRepository.findTopByCommunityAndIsCreator(community, true).orElse(null));
        membershipRepository.save(membership);
    }
    public void leaveCommunity(String groupname) {
        var membership = getMembershipOrThrowErr(userMethodsService.getCurrentUser(), communityMethodsService.getCommunityByNameOrThrowErr(groupname));
        membershipRepository.delete(membership);
    }

    public ArrayList<PublicCommunityCardDto> getJoinedCommunitiesByNickname(String nickname) {
        var user = userMethodsService.getUserByNickname(nickname);
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


    public MembersDto getMembersByCommunity(String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (community.isClosed() && !communityMethodsService.isContextUserMember(community)) {
            return MembersDto.builder()
                    .isContextUserAccess(false)
                    .build();
        }
        var memberships = getMembershipsByCommunity(community, false);
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
        var roles = new ArrayList<String>();
        for (CommunityRole r : communityRoleRepository.findAllByCommunity(community)) {
            if (!r.isCreator())
                roles.add(r.getTitle());
        }
        return MembersDto.builder()
                .users(users)
                .roles(roles)
                .isContextUserAccess(true)
                .build();
    }

    public void banUser(String groupname, String user) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var userEntity = userMethodsService.getUserByNickname(user);
        var userMembership = getMembershipOrThrowErr(userEntity, community);
        var admin = userMethodsService.getCurrentUser();
        var adminMembership = getMembershipOrThrowErr(admin, community);

        if ((userMembership.getRole() == null && adminMembership.getRole().isBanUser())
                || (userMembership.getRole().isCitizen() && adminMembership.getRole().isBanCitizen())
                || (community.getOwner().equals(admin))
        ) {
            userMembership.setBanned(true);
            userMembership.setRole(null);
            membershipRepository.save(userMembership);
        }
        else {
            throw new UnauthorizedException("No permissions to ban user");
        }
    }

    public void grantRole(String nickname, String groupname, String roleName) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (communityMethodsService.isCurrentUserOwner(community)) {
            var role = communityMethodsService.findRoleOrThrowErr(community, roleName);
            var membership = getMembershipOrThrowErr(userMethodsService.getUserByNickname(nickname), community);
            membership.setRole(role);
            membershipRepository.save(membership);
        }
    }

    public void revokeRole(String nickname, String groupname) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        if (communityMethodsService.isCurrentUserOwner(community)) {
            var membership = getMembershipOrThrowErr(userMethodsService.getUserByNickname(nickname), community);
            membership.setRole(null);
            membershipRepository.save(membership);
        }
    }

}
