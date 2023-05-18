package ed.back_snekhome.services;

import ed.back_snekhome.dto.*;
import ed.back_snekhome.email.ConfirmationToken;
import ed.back_snekhome.email.ConfirmationTokenService;
import ed.back_snekhome.email.ConfirmationType;
import ed.back_snekhome.email.EmailSendService;
import ed.back_snekhome.entities.InfoTag;
import ed.back_snekhome.entities.UserEntity;
import ed.back_snekhome.entities.UserImage;
import ed.back_snekhome.exceptionHandler.exceptions.*;
import ed.back_snekhome.repositories.InfoTagRepository;
import ed.back_snekhome.repositories.UserImageRepository;
import ed.back_snekhome.repositories.UserRepository;
import ed.back_snekhome.response.AuthenticationResponse;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.security.JwtService;
import ed.back_snekhome.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSendService emailSendService;
    private final InfoTagRepository infoTagRepository;
    private final FileService fileService;
    private final UserImageRepository userImageRepository;


    public AuthenticationResponse loginUser(LoginDto loginDto) {

        var user = userRepository.findByEmail(loginDto.getLogin())
                .orElseThrow( () -> new LoginNotFoundException("Account with given email is not found"));

        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getLogin(), loginDto.getPassword());

        authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        //if user is authenticated:

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build();
    }

    public void saveNewAccount(RegisterDto registerDto) {

        throwErrIfExistsByEmail(registerDto.getEmail());
        throwErrIfExistsByNickname(registerDto.getNickname());

        var userEntity = UserEntity.builder()
                .password( passwordEncoder.encode(registerDto.getPassword()) )
                .name( registerDto.getName() )
                .surname( registerDto.getSurname() )
                .email( registerDto.getEmail() )
                .nickname( registerDto.getNickname() )
                .registration( java.time.LocalDate.now() )
                .role( Role.USER )
                .enabled( false )
                .build();

        userRepository.save(userEntity);

        sendVerificationMail(userEntity);

    }

    public String confirmToken(String tokenValue) {
        var token = confirmationTokenService.findByToken(tokenValue);

        var type = token.getConfirmationType();
        String message = "";
        switch (type) {
            case REGISTRATION -> {
                activateAccountIfVerified(token);
                message = "Account is activated";
            }
            case PASSWORD_RESET -> {
                passwordReset(token);
                message = "Reset password email is sent";
            }
            case CHANGE_EMAIL -> {
                changeEmailActionConfirmation(token);
                message = "Verification is sent on new email address";
            }
            case CHANGE_EMAIL_LAST -> {
                changeEmailLastConfirmation(token);
                message = "Email changed successfully";
            }

        }
        return message;
    }


    private void activateAccountIfVerified(ConfirmationToken token) {

        var account = getUserById( token.getIdUser() );

        if ( !token.isNotExpired() ){
            sendVerificationMail( getUserById( token.getIdUser() ) );
            throw new TokenExpiredException("Verification token is expired, new one is sent on your e-mail");
        }

        account.setEnabled(true);
        userRepository.save(account);
    }

    private void changeEmailActionConfirmation(ConfirmationToken token) { //confirms action "change email"
        var account = getUserById(token.getIdUser());
        throwErrIfTokenExpired(token);

        var newToken = new ConfirmationToken(
                account.getIdAccount(),
                ConfirmationType.CHANGE_EMAIL_LAST,
                15,
                45,
                token.getMessage()); //message stores new email address
        confirmationTokenService.save(newToken);

        emailSendService.sendNewEmailConfirmationMail(
                newToken.getMessage(),
                account.getName(),
                newToken.getToken()
        );
    }

    private void changeEmailLastConfirmation(ConfirmationToken token) { //new email confirmation
        var account = getUserById(token.getIdUser());
        throwErrIfTokenExpired(token);

        account.setEmail(token.getMessage());
        userRepository.save(account);
    }

    private void passwordReset(ConfirmationToken token) {

    }


    public void sendVerificationMail( UserEntity user ) {

        var confirmationToken = new ConfirmationToken( user.getIdAccount(), ConfirmationType.REGISTRATION, 15, 45 );
        confirmationTokenService.save(confirmationToken);

        emailSendService.sendVerificationMail( user.getEmail(), user.getName(), confirmationToken.getToken() );
    }

    public void changeEmail(String email) { //sends email list on old email to confirm action

        throwErrIfExistsByEmail(email);
        var user = getCurrentUser();
        var confirmationToken = new ConfirmationToken(
                user.getIdAccount(),
                ConfirmationType.CHANGE_EMAIL,
                15, 45,
                email);
        confirmationTokenService.save(confirmationToken);
        emailSendService.sendChangeEmailMail(
                user.getEmail(),
                email,
                user.getName(),
                confirmationToken.getToken()
        );
    }

    public void changePassword(ChangePasswordDto dto) {
        var user = getCurrentUser();
        if ( !passwordEncoder.matches(dto.getOldPass(), user.getPassword()) ) {
            throw new PasswordDoesntMatchException("Invalid old password, please try again");
        }
        //if old password matches:
        user.setPassword(passwordEncoder.encode(dto.getNewPass()));
        userRepository.save(user);
    }


    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }
    public UserEntity getUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }
    private UserEntity getCurrentUser() {
        return userRepository.findByEmail( SecurityContextHolder.getContext().getAuthentication().getName() )
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }
    public boolean isCurrentUserEqual(UserEntity user2) {
        if (getCurrentUser().equals(user2))
            return true;
        else
            throw new UnauthorizedException("Entity is not belonged to authorized user");
    }
    private void throwErrIfExistsByNickname(String nickname) {
        if ( userRepository.existsByNickname(nickname) ) {
            throw new UserAlreadyExistsException("Name: " + nickname + " is already taken"); /*|| communitiesRepository.existsByNickname(registerDto.getNickname())*/
        }
    }
    private void throwErrIfExistsByEmail(String email) {
        if ( userRepository.existsByEmail(email) ) {
            throw new UserAlreadyExistsException("User with email: " + email + " exists");
        }
    }
    private void throwErrIfTokenExpired(ConfirmationToken token) {
        if ( !token.isNotExpired() ) {
            throw new TokenExpiredException("Confirmation token is expired");
        }
    }


    public void updateUser(UserUpdateDto userUpdateDto) {
        var user = getCurrentUser();

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
            throwErrIfExistsByNickname(userUpdateDto.getNickname());
            user.setNickname(userUpdateDto.getNickname());
        }


        userRepository.save(user);
    }

    public OwnSuccessResponse uploadUserImage(MultipartFile file) throws IOException {

        String newName = fileService.uploadImageNameReturned(file);
        var userImage = UserImage.builder()
                .name(newName)
                .user(getCurrentUser())
                .build();
        userImageRepository.save(userImage);

        return new OwnSuccessResponse( newName ); //returns new name of uploaded file
    }


    public UserPublicDto getNavbarInfo() {

        var currentUser = getCurrentUser();

        return UserPublicDto.builder()
                .image( getTopImageOfUser(currentUser.getImages()) )
                .nickname( currentUser.getNickname() )
                .nicknameColor( currentUser.getNicknameColor() )
                .build();
    }



    public UserPublicDto getUserInfo(String nickname) {

        var user = getUserByNickname(nickname);

        return UserPublicDto.builder()
                .image( getTopImageOfUser(user.getImages()) )
                .nickname( user.getNickname() )
                .nicknameColor( user.getNicknameColor() )
                .name( user.getName() )
                .surname( user.getSurname() )
                .friends( 56 )
                .communities( 8 )
                .tags( user.getTags() )
                .build();
    }


    public UserPrivateDto getCurrentUserInfo() {
        var currentUser = getCurrentUser();

        return UserPrivateDto.builder()
                .email( currentUser.getEmail() )
                .name( currentUser.getName() )
                .surname( currentUser.getSurname() )
                .tags( currentUser.getTags() )
                .build();
    }


    private String getTopImageOfUser(List<UserImage> images) {
        if (images.size() == 0) {
            return "";
        }
        else {
            return  images.get( images.size() - 1 ).getName();
        }
    }

    public void newTag(TagDto tagDto) {
        var infoTag = InfoTag.builder()
                .user( getCurrentUser() )
                .title( tagDto.getTitle() )
                .text(tagDto.getText() )
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

        if ( isCurrentUserEqual(tag.getUser()) ) {
            infoTagRepository.delete(tag);
        }
    }


}

