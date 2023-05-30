package ed.back_snekhome.services;

import ed.back_snekhome.entities.relations.Friendship;
import ed.back_snekhome.repositories.FriendshipRepository;
import ed.back_snekhome.repositories.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RelationsService {

    private final UserService userService;
    private final CommunityService communityService;
    private final FriendshipRepository friendshipRepository;
    private final MembershipRepository membershipRepository;


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


}
