package ed.back_snekhome.services;

import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.user.Friendship;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.repositories.user.FriendshipRepository;
import ed.back_snekhome.helperComponents.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final UserHelper userHelper;
    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;

    public void addFriend(String nickname) {
        manageFriend(nickname, true);
    }

    public void delFriend(String nickname) {
        manageFriend(nickname, false);
    }

    private void manageFriend(String nickname, boolean isAdd) {

        var requestUser = userHelper.getCurrentUser();
        var secondUser = userHelper.getUserByNicknameOrThrowErr(nickname);
        var friendship = getFriendshipOrCreate(requestUser.getIdAccount(), secondUser.getIdAccount());

        if (isAdd)
            notificationService.createAddFriendNotification(secondUser, requestUser);

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

    private List<Friendship> getFriendshipsByUserId(Long id) {
        return friendshipRepository.findAllByIdFirstUserOrIdSecondUser(id, id);
    }

    public List<UserPublicDto> getFriends(String nickname) {
        var user = userHelper.getUserByNicknameOrThrowErr(nickname);
        var friendships = getFriendshipsByUserId(user.getIdAccount());

        return friendships.stream().map(u -> {
            UserEntity friend;
            if (u.getIdFirstUser().equals(user.getIdAccount()))
                friend = userHelper.getUserByIdOrThrowErr(u.getIdSecondUser());
            else
                friend = userHelper.getUserByIdOrThrowErr(u.getIdFirstUser());
            return UserPublicDto.builder()
                    .image(userHelper.getTopUserImage(friend))
                    .nickname(friend.getNickname())
                    .friendshipType(userHelper.getFriendshipType(user.getIdAccount(), friend.getIdAccount()))
                    .name(friend.getName())
                    .surname(friend.getSurname())
                    .build();
        }).toList();
    }

}
