package ed.back_snekhome.services;

import ed.back_snekhome.dto.userDTOs.*;
import ed.back_snekhome.entities.user.InfoTag;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.entities.user.UserImage;
import ed.back_snekhome.exceptionHandler.exceptions.*;
import ed.back_snekhome.repositories.community.MembershipRepository;
import ed.back_snekhome.repositories.user.FriendshipRepository;
import ed.back_snekhome.repositories.user.InfoTagRepository;
import ed.back_snekhome.repositories.user.UserImageRepository;
import ed.back_snekhome.repositories.user.UserRepository;
import ed.back_snekhome.response.OwnSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
@RequiredArgsConstructor
public class UserService {


    private final UserMethodsService userMethodsService;
    private final UserRepository userRepository;
    private final InfoTagRepository infoTagRepository;
    private final FileService fileService;
    private final UserImageRepository userImageRepository;
    private final FriendshipRepository friendshipRepository;
    private final MembershipRepository membershipRepository;


    public void updateUser(UserUpdateDto userUpdateDto) {
        var user = userMethodsService.getCurrentUser();

        if (userUpdateDto.getNicknameColor() != null) {
            user.setNicknameColor(userUpdateDto.getNicknameColor());
        }
        else if (userUpdateDto.getName() != null) {
            user.setName(userUpdateDto.getName());
        }
        else if (userUpdateDto.getSurname() != null) {
            user.setSurname(userUpdateDto.getSurname());
        }
        else if (userUpdateDto.getNickname() != null) {
            userMethodsService.throwErrIfExistsByNickname(userUpdateDto.getNickname());
            user.setNickname(userUpdateDto.getNickname());
        }

        userRepository.save(user);
    }

    public OwnSuccessResponse uploadUserImage(MultipartFile file) throws IOException {

        String newName = fileService.uploadImageNameReturned(file);
        var userImage = UserImage.builder()
                .name(newName)
                .user(userMethodsService.getCurrentUser())
                .build();
        userImageRepository.save(userImage);

        return new OwnSuccessResponse(newName); //returns new name of uploaded file
    }


    public UserPublicDto getNavbarInfo() {
        var currentUser = userMethodsService.getCurrentUser();

        return UserPublicDto.builder()
                .image(userMethodsService.getTopUserImage(currentUser))
                .nickname(currentUser.getNickname())
                .nicknameColor(currentUser.getNicknameColor())
                .build();
    }

    private int countFriends(Long id) {
        int friends = 0;
        friends += friendshipRepository.countAllByIdFirstUserAndIsFirstUserAndIsSecondUser(id, true, true);
        friends += friendshipRepository.countAllByIdSecondUserAndIsFirstUserAndIsSecondUser(id, true, true);
        return friends;
    }
    private int countCommunities(UserEntity user) {
        int communities = 0;
        communities += membershipRepository.countAllByUserAndIsBanned(user, false);
        return communities;
    }


    public UserPublicDto getUserInfo(String nickname) {

        var user = userMethodsService.getUserByNicknameOrThrowErr(nickname);

        var dto = UserPublicDto.builder()
                .images(user.getImages())
                .nickname(user.getNickname())
                .nicknameColor(user.getNicknameColor())
                .name(user.getName())
                .surname(user.getSurname())
                .friends(countFriends(user.getIdAccount()))
                .communities(countCommunities(user))
                .tags(user.getTags())
                .build();

        //Checks the relation between current user and related one: are friends/aren't friends/context user follows related/related follows context user
        if (userMethodsService.isContextUser() && !userMethodsService.getCurrentUser().getNickname().equals(nickname)) {
            dto.setFriendshipType(
                    userMethodsService.getFriendshipType(userMethodsService.getCurrentUser().getIdAccount(),
                            user.getIdAccount()));
        }
        return dto;
    }

    public UserPrivateDto getCurrentUserInfo() {
        var currentUser = userMethodsService.getCurrentUser();

        return UserPrivateDto.builder()
                .email(currentUser.getEmail())
                .name(currentUser.getName())
                .surname(currentUser.getSurname())
                .tags(currentUser.getTags())
                .build();
    }


    public void newTag(TagDto tagDto) {
        var infoTag = InfoTag.builder()
                .user(userMethodsService.getCurrentUser())
                .title(tagDto.getTitle())
                .text(tagDto.getText())
                .build();

        infoTagRepository.save(infoTag);
    }
    public void updateTag(TagDto tagDto) {
        var tag = infoTagRepository.findById(tagDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Info tag is not found"));
        tag.setText(tagDto.getText());
        tag.setTitle(tagDto.getTitle());
        infoTagRepository.save(tag);
    }
    public void delTag(Long id) {
        var tag = infoTagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Info tag is not found"));

        if (userMethodsService.isCurrentUserEqual(tag.getUser()))
            infoTagRepository.delete(tag);
        else
            throw new UnauthorizedException("Entity is not belonged to authorized user");
    }


}

