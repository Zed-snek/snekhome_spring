package ed.back_snekhome.helperComponents;

import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.FriendshipType;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UserAlreadyExistsException;
import ed.back_snekhome.repositories.community.CommunityRepository;
import ed.back_snekhome.repositories.user.FriendshipRepository;
import ed.back_snekhome.repositories.user.UserImageRepository;
import ed.back_snekhome.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHelper {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final FriendshipRepository friendshipRepository;

    public UserEntity getUserByIdOrThrowErr(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }

    public UserEntity getUserByNicknameOrThrowErr(String nickname) {
        return userRepository.findByNicknameIgnoreCase(nickname)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }

    public UserEntity getCurrentUser() {
        return userRepository.findByEmailIgnoreCase(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }

    public boolean isContextUser() {
        String s = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray()[0].toString();
        return !s.equals("ROLE_ANONYMOUS");
    }

    public boolean isCurrentUserEqual(UserEntity user2) {
        return isContextUser() && getCurrentUser().equals(user2);
    }

    public void throwErrIfExistsByNickname(String nickname) {
        if (userRepository.existsByNicknameIgnoreCase(nickname)
                || communityRepository.existsByGroupnameIgnoreCase(nickname)) {
            throw new UserAlreadyExistsException("Nickname: " + nickname + " is already taken");
        }
    }

    public FriendshipType getFriendshipType(Long idUser1, Long idUser2) {
        var friendship
                = friendshipRepository.findFriendshipByIdFirstUserAndIdSecondUser(idUser1, idUser2);
        if (friendship.isEmpty()) {
            friendship = friendshipRepository.findFriendshipByIdFirstUserAndIdSecondUser(idUser2, idUser1);
        }
        if (friendship.isPresent()) {
            if (friendship.get().isFirstUser() && friendship.get().isSecondUser())
                return FriendshipType.FRIENDS;
            else if (!friendship.get().isFirstUser() && !friendship.get().isSecondUser())
                return FriendshipType.NOT_FRIENDS;
            else if (friendship.get().getIdFirstUser().equals(idUser1) && friendship.get().isFirstUser()
                    || friendship.get().getIdSecondUser().equals(idUser1) && friendship.get().isSecondUser())
                return FriendshipType.CURRENT_FOLLOW;
            else
                return FriendshipType.SECOND_FOLLOW;
        }
        else
            return FriendshipType.NOT_FRIENDS;
    }

    public String getTopUserImage(UserEntity user) {
        var img = userImageRepository.findTopByUserOrderByIdImageDesc(user);
        if (img.isPresent())
            return img.get().getName();
        return "";
    }


}
