package ed.back_snekhome.services;

import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.FriendshipType;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UserAlreadyExistsException;
import ed.back_snekhome.repositories.CommunityRepository;
import ed.back_snekhome.repositories.FriendshipRepository;
import ed.back_snekhome.repositories.UserImageRepository;
import ed.back_snekhome.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMethodsService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final FriendshipRepository friendshipRepository;

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }
    public UserEntity getUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }
    public UserEntity getCurrentUser() {
        return userRepository.findByEmail( SecurityContextHolder.getContext().getAuthentication().getName() )
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }
    public boolean isContextUser() {
        String s = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray()[0].toString();
        return !s.equals("ROLE_ANONYMOUS");
    }
    public boolean isCurrentUserEqual(UserEntity user2) {
        return getCurrentUser().equals(user2);
    }
    public void throwErrIfExistsByNickname(String nickname) {
        if ( userRepository.existsByNickname(nickname) || communityRepository.existsByGroupname(nickname) ) {
            throw new UserAlreadyExistsException("Name: " + nickname + " is already taken");
        }
    }

    public FriendshipType getFriendshipType(Long idUser1, Long idUser2) {
        var friendship = friendshipRepository.findFriendshipByIdFirstUserAndIdSecondUser(idUser1, idUser2);
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
