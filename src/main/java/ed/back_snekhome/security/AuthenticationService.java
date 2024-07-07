package ed.back_snekhome.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import ed.back_snekhome.dto.userDTOs.ChangePasswordDto;
import ed.back_snekhome.dto.userDTOs.LoginDto;
import ed.back_snekhome.dto.userDTOs.RegisterDto;
import ed.back_snekhome.dto.userDTOs.ResetPasswordDto;
import ed.back_snekhome.email.ConfirmationToken;
import ed.back_snekhome.email.ConfirmationTokenService;
import ed.back_snekhome.email.ConfirmationType;
import ed.back_snekhome.email.EmailSendService;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.exceptionHandler.exceptions.*;
import ed.back_snekhome.repositories.user.UserRepository;
import ed.back_snekhome.response.AuthenticationResponse;
import ed.back_snekhome.helperComponents.UserHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    @SneakyThrows
    public void refreshJwtToken(HttpServletRequest request, HttpServletResponse response) {
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


    @Transactional
    public ConfirmationType confirmToken(String tokenValue) {
        var token = confirmationTokenService.findByToken(tokenValue);

        var type = token.getConfirmationType();
        switch (type) {
            case REGISTRATION -> activateAccountIfVerified(token);
            case PASSWORD_RESET ->  confirmationTokenService.throwErrIfTokenExpiredOrActivated(token);
            case CHANGE_EMAIL -> changeEmailActionConfirmation(token);
            case CHANGE_EMAIL_LAST -> changeEmailLastConfirmation(token);
        }
        return type;
    }


    private void activateAccountIfVerified(ConfirmationToken token) {
        var account = token.getUser();
        confirmationTokenService.throwErrIfTokenIsActivated(token);
        if (token.isExpired()) {
            sendVerificationMail(account);
            throw new TokenExpiredException("Verification token is expired, new one is sent on your e-mail");
        }
        confirmationTokenService.activateToken(token);
        account.setEnabled(true);
        userRepository.save(account);
    }


    private void changeEmailActionConfirmation(ConfirmationToken token) { //confirms action "change email"
        var account = token.getUser();
        confirmationTokenService.throwErrIfTokenExpiredOrActivated(token);
        confirmationTokenService.activateToken(token);

        var newToken = new ConfirmationToken(
                account,
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
        var account = token.getUser();
        confirmationTokenService.throwErrIfTokenExpiredOrActivated(token);
        confirmationTokenService.activateToken(token);

        account.setEmail(token.getMessage());
        userRepository.save(account);
    }


    private void throwErrIfExistsByEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email))
            throw new UserAlreadyExistsException("User with email: " + email + " exists");
    }


    @Transactional
    public void resetPassword(ResetPasswordDto dto) {
        var token = confirmationTokenService.findByToken(dto.getToken());
        if (token.getConfirmationType() != ConfirmationType.PASSWORD_RESET)
            throw new BadRequestException("Token type doesn't match");
        confirmationTokenService.throwErrIfTokenExpiredOrActivated(token);
        confirmationTokenService.activateToken(token);

        var user = token.getUser();
        user.setPassword(passwordEncoder.encode(dto.getNewPass()));
        userRepository.save(user);
    }


    public void sendResetPasswordMail(String email) {
        var user = getUserByEmailOrThrowErr(email);

        var confirmationToken = new ConfirmationToken(user, ConfirmationType.PASSWORD_RESET, 15, 45);
        confirmationTokenService.save(confirmationToken);

        emailSendService.sendResetPasswordMail(user.getEmail(), user.getName(), confirmationToken.getToken());
    }


    public void sendVerificationMail(UserEntity user) {

        var confirmationToken = new ConfirmationToken(user, ConfirmationType.REGISTRATION, 15, 45);
        confirmationTokenService.save(confirmationToken);

        emailSendService.sendVerificationMail(user.getEmail(), user.getName(), confirmationToken.getToken());
    }


    public void changeEmail(String email) { //sends email list on old email to confirm action
        throwErrIfExistsByEmail(email);
        var user = userHelper.getCurrentUser();
        var confirmationToken = new ConfirmationToken(
                user,
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
        if (!passwordEncoder.matches(dto.getOldPass(), user.getPassword()))
            throw new PasswordDoesntMatchException("Invalid old password, please try again");

        user.setPassword(passwordEncoder.encode(dto.getNewPass()));
        userRepository.save(user);
    }

}
