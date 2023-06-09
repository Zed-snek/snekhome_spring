package ed.back_snekhome.services;

import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.Community;
import ed.back_snekhome.entities.UserEntity;
import ed.back_snekhome.entities.relations.Friendship;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
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

    private Iterable<Friendship> getFriendshipByUserId(Long id) {
        return friendshipRepository.findAllByIdFirstUserOrIdSecondUser(id, id);
    }

    public ArrayList<UserPublicDto> getFriends(String nickname) {
        var user = userService.getUserByNickname(nickname);
        var friendships = getFriendshipByUserId(user.getIdAccount());
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
                            .friendshipType(userService.getFriendshipType(f.getIdFirstUser(), f.getIdSecondUser()))
                            .name(friend.getName())
                            .surname(friend.getSurname())
                    .build());
        }
        return array;
    }



    public Membership getMembership(UserEntity user, Community community) {
        return membershipRepository.findByCommunityAndUser(community, user).orElseThrow(() -> new EntityNotFoundException("User is not a member"));
    }

    public void joinCommunity(String groupname) {
        var current = userService.getCurrentUser();
        var community = communityService.getCommunityByName(groupname);
        var membership = Membership.builder()
                .user( current )
                .community( community )
                .build();
        if (community.getOwner().equals(current))
            membership.setRole(communityRoleRepository.findTopByCommunityAndIsCreator(community, true).orElse(null));
        membershipRepository.save(membership);
    }
    public void leaveCommunity(String groupname) {
        var membership = getMembership(userService.getCurrentUser(), communityService.getCommunityByName(groupname));
        membershipRepository.delete(membership);
    }




}
