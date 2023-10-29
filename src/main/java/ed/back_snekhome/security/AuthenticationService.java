package ed.back_snekhome.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import ed.back_snekhome.dto.userDTOs.ChangePasswordDto;
import ed.back_snekhome.dto.userDTOs.LoginDto;
import ed.back_snekhome.dto.userDTOs.RegisterDto;
import ed.back_snekhome.email.ConfirmationToken;
import ed.back_snekhome.email.ConfirmationTokenService;
import ed.back_snekhome.email.ConfirmationType;
import ed.back_snekhome.email.EmailSendService;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.exceptionHandler.exceptions.LoginNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.PasswordDoesntMatchException;
import ed.back_snekhome.exceptionHandler.exceptions.TokenExpiredException;
import ed.back_snekhome.exceptionHandler.exceptions.UserAlreadyExistsException;
import ed.back_snekhome.repositories.user.UserRepository;
import ed.back_snekhome.response.AuthenticationResponse;
import ed.back_snekhome.helperComponents.UserHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSendService emailSendService;
    private final UserHelper userHelper;
    private final UserRepository userRepository;

    public AuthenticationResponse loginUser(LoginDto loginDto) {

        var user = getUserByEmailOrThrowErr(loginDto.getLogin());
        var usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getLogin(), loginDto.getPassword());

        authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        //if user is authenticated:

        var jwtToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return;
        final String refreshToken = authHeader.replace("Bearer ", "");
        final String userEmail = jwtService.extractEmail(refreshToken);

        if (userEmail != null) {
            var userDetails = getUserByEmailOrThrowErr(userEmail);
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                var authResponse = AuthenticationResponse
                        .builder()
                        .token(jwtService.generateAccessToken(userDetails))
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    private UserEntity getUserByEmailOrThrowErr(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new LoginNotFoundException("Account with given email is not found"));
    }

    public void saveNewAccount(RegisterDto registerDto) {
        throwErrIfExistsByEmail(registerDto.getEmail());
        userHelper.throwErrIfExistsByNickname(registerDto.getNickname());

        var userEntity = UserEntity.builder()
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .name(registerDto.getName())
                .surname(registerDto.getSurname())
                .email(registerDto.getEmail())
                .nickname(registerDto.getNickname())
                .registration(java.time.LocalDate.now())
                .role(Role.USER)
                .enabled(false)
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
        var account = userHelper.getUserByIdOrThrowErr( token.getIdUser() );
        if (!token.isNotExpired()){
            sendVerificationMail(account);
            throw new TokenExpiredException("Verification token is expired, new one is sent on your e-mail");
        }
        account.setEnabled(true);
        userRepository.save(account);
    }

    private void changeEmailActionConfirmation(ConfirmationToken token) { //confirms action "change email"
        var account = userHelper.getUserByIdOrThrowErr(token.getIdUser());
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
        var account = userHelper.getUserByIdOrThrowErr(token.getIdUser());
        throwErrIfTokenExpired(token);

        account.setEmail(token.getMessage());
        userRepository.save(account);
    }

    private void throwErrIfExistsByEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException("User with email: " + email + " exists");
        }
    }
    private void throwErrIfTokenExpired(ConfirmationToken token) {
        if (!token.isNotExpired()) {
            throw new TokenExpiredException("Confirmation token is expired");
        }
    }

    private void passwordReset(ConfirmationToken token) {

    }


    public void sendVerificationMail( UserEntity user ) {

        var confirmationToken = new ConfirmationToken(user.getIdAccount(), ConfirmationType.REGISTRATION, 15, 45);
        confirmationTokenService.save(confirmationToken);

        emailSendService.sendVerificationMail( user.getEmail(), user.getName(), confirmationToken.getToken() );
    }

    public void changeEmail(String email) { //sends email list on old email to confirm action

        throwErrIfExistsByEmail(email);
        var user = userHelper.getCurrentUser();
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
        var user = userHelper.getCurrentUser();
        if (!passwordEncoder.matches(dto.getOldPass(), user.getPassword())) {
            throw new PasswordDoesntMatchException("Invalid old password, please try again");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPass()));
        userRepository.save(user);
    }

}
