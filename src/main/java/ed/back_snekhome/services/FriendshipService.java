package ed.back_snekhome.services;

import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.user.Friendship;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.repositories.user.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final UserMethodsService userMethodsService;
    private final FriendshipRepository friendshipRepository;

    public void addFriend(String nickname) {
        manageFriend(nickname, true);
    }
    public void delFriend(String nickname) {
        manageFriend(nickname, false);
    }
    private void manageFriend(String nickname, boolean isAdd) {

        var requestUser = userMethodsService.getCurrentUser();
        var secondUser = userMethodsService.getUserByNicknameOrThrowErr(nickname);
        var friendship = getFriendshipOrCreate(requestUser.getIdAccount(), secondUser.getIdAccount());

        if (Objects.equals(friendship.getIdFirstUser(), requestUser.getIdAccount()))
            friendship.setFirstUser(isAdd);
        else
            friendship.setSecondUser(isAdd);
        friendshipRepository.save(friendship);
    }

    public Friendship getFriendshipOrCreate(Long firstUser, Long secondUser) {
        var friendship
                = friendshipRepository.findFriendshipByIdFirstUserAndIdSecondUser(firstUser, secondUser);
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
        var user = userMethodsService.getUserByNicknameOrThrowErr(nickname);
        var friendships = getFriendshipsByUserId(user.getIdAccount());
        var array = new ArrayList<UserPublicDto>();
        for (Friendship f : friendships) {
            UserEntity friend;
            if (f.getIdFirstUser().equals(user.getIdAccount()))
                friend = userMethodsService.getUserByIdOrThrowErr(f.getIdSecondUser());
            else
                friend = userMethodsService.getUserByIdOrThrowErr(f.getIdFirstUser());
            array.add(UserPublicDto.builder()
                    .image(userMethodsService.getTopUserImage(friend))
                    .nickname(friend.getNickname())
                    .friendshipType(
                            userMethodsService.getFriendshipType(user.getIdAccount(), friend.getIdAccount())
                    )
                    .name(friend.getName())
                    .surname(friend.getSurname())
                    .build());
        }
        return array;
    }

}
