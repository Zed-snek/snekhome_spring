package ed.back_snekhome.services;

import ed.back_snekhome.dto.MembersDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityCardDto;
import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.Community;
import ed.back_snekhome.entities.CommunityRole;
import ed.back_snekhome.entities.UserEntity;
import ed.back_snekhome.entities.relations.Friendship;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.CommunityRoleRepository;
import ed.back_snekhome.repositories.FriendshipRepository;
import ed.back_snekhome.repositories.MembershipRepository;
import ed.back_snekhome.utils.ListFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RelationsService {

    private final UserService userService;
    private final CommunityService communityService;
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

        var requestUser = userService.getCurrentUser();
        var secondUser = userService.getUserByNickname(nickname);
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
        var user = userService.getUserByNickname(nickname);
        var friendships = getFriendshipsByUserId(user.getIdAccount());
        var array = new ArrayList<UserPublicDto>();
        for (Friendship f : friendships) {
            UserEntity friend;
            if (f.getIdFirstUser().equals(user.getIdAccount()))
                friend = userService.getUserById(f.getIdSecondUser());
            else
                friend = userService.getUserById(f.getIdFirstUser());
            array.add(UserPublicDto.builder()
                            .image(ListFunctions.getTopImageOfList(friend.getImages()))
                            .nickname(friend.getNickname())
                            .friendshipType(userService.getFriendshipType(user.getIdAccount(), friend.getIdAccount()))
                            .name(friend.getName())
                            .surname(friend.getSurname())
                    .build());
        }
        return array;
    }



    public Membership getMembership(UserEntity user, Community community) {
        return membershipRepository.findByCommunityAndUser(community, user).orElseThrow(() -> new EntityNotFoundException("User is not a member"));
    }

    public Iterable<Membership> getMembershipsByUser(UserEntity user, boolean isBanned) {
        return membershipRepository.findAllByUserAndIsBanned(user, isBanned);
    }

    public Iterable<Membership> getMembershipsByCommunity(Community community, boolean isBanned) {
        return membershipRepository.findAllByCommunityAndIsBanned(community, isBanned);
    }

    public void joinCommunity(String groupname) {
        var current = userService.getCurrentUser();
        var community = communityService.getCommunityByName(groupname);
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
        var membership = getMembership(userService.getCurrentUser(), communityService.getCommunityByName(groupname));
        membershipRepository.delete(membership);
    }

    public ArrayList<PublicCommunityCardDto> getJoinedCommunitiesByNickname(String nickname) {
        var user = userService.getUserByNickname(nickname);
        var memberships = getMembershipsByUser(user, false);
        var array = new ArrayList<PublicCommunityCardDto>();
        memberships.forEach(
                m -> array.add(PublicCommunityCardDto.builder()
                        .image( ListFunctions.getTopImageOfList(m.getCommunity().getImages()) )
                        .name( m.getCommunity().getName() )
                        .groupname( m.getCommunity().getGroupname() )
                        .description( m.getCommunity().getDescription() )
                        .members( communityService.countMembers(m.getCommunity()) )
                        .build())
        );
        return array;
    }


    public MembersDto getMembersByCommunity(String groupname) {
        var community = communityService.getCommunityByName(groupname);
        if (community.isClosed() && !communityService.isContextUserMember(community)) {
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
                            .image(ListFunctions.getTopImageOfList(user.getImages()))
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
        var community = communityService.getCommunityByName(groupname);
        var userEntity = userService.getUserByNickname(user);
        var userMembership = getMembership(userEntity, community);
        var admin = userService.getCurrentUser();
        var adminMembership = getMembership(admin, community);

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
        var community = communityService.getCommunityByName(groupname);
        if (communityService.isCurrentUserOwner(community)) {
            var role = communityService.findRoleOrThrowErr(community, roleName);
            var membership = getMembership(userService.getUserByNickname(nickname), community);
            membership.setRole(role);
            membershipRepository.save(membership);
        }
    }

    public void revokeRole(String nickname, String groupname) {
        var community = communityService.getCommunityByName(groupname);
        if (communityService.isCurrentUserOwner(community)) {
            var membership = getMembership(userService.getUserByNickname(nickname), community);
            membership.setRole(null);
            membershipRepository.save(membership);
        }
    }

}
