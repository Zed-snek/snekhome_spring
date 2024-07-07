package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.userDTOs.*;
import ed.back_snekhome.response.AuthenticationResponse;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.security.AuthenticationService;
import ed.back_snekhome.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserManagementController {


    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/user/login")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@RequestBody LoginDto loginDto) {

        log.info("User " + loginDto.getLogin() + " is logging");
        AuthenticationResponse response = authenticationService.loginUser(loginDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/user/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        log.info("Jwt token is refreshing");
        authenticationService.refreshJwtToken(request, response);
    }


    @PostMapping("/user/register")
    public ResponseEntity<OwnSuccessResponse> registerUser(@Valid @RequestBody RegisterDto registerDto) {

        log.info("User is registering: " + registerDto.getEmail());
        authenticationService.saveNewAccount(registerDto);
        var response = new OwnSuccessResponse("Confirm registration on email");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/user/reset_password")
    public ResponseEntity<OwnSuccessResponse> sendResetPasswordMail(@RequestBody EmailDto dto) {

        log.info("Sending reset password mail to: " + dto.getEmail());
        authenticationService.sendResetPasswordMail(dto.getEmail());

        var response = new OwnSuccessResponse("Email is successfully sent");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/user/reset_password")
    public ResponseEntity<OwnSuccessResponse> resetPassword(@RequestBody ResetPasswordDto dto) {

        log.info("Password is resetting");
        authenticationService.resetPassword(dto);

        var response = new OwnSuccessResponse("Password has been changed");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/user/confirmation")
    public ResponseEntity<OwnSuccessResponse> confirmationManager(@RequestParam String token) {

        log.info("Token is requested to confirm: " + token);
        var response = new OwnSuccessResponse(authenticationService.confirmToken(token).name()); //method confirmToken() returns a message

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/auth/user/password")
    public ResponseEntity<OwnSuccessResponse> updatePassword(@Valid @RequestBody ChangePasswordDto dto) {

        log.info("Password is updating");
        authenticationService.changePassword(dto);
        var response = new OwnSuccessResponse("Password has been changed");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/auth/user/email")
    public ResponseEntity<OwnSuccessResponse> updateEmail(@RequestBody EmailDto dto) {

        log.info("Email is updating, new: " + dto.getEmail());
        authenticationService.changeEmail(dto.getEmail());
        var response = new OwnSuccessResponse("Check your email address to change to confirm action");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/auth/user/current")
    public ResponseEntity<OwnSuccessResponse> updateUser(@RequestBody UserUpdateDto dto) {

        log.info("User requested to update profile, new values: " + dto);
        userService.updateUser(dto);
        var response = new OwnSuccessResponse("User has been updated");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping(path = "/auth/user/current/image", consumes = "multipart/form-data")
    public ResponseEntity<OwnSuccessResponse> newImage(@RequestParam("image") MultipartFile image) throws IOException {

        log.info("Uploading a new image");
        var newName = userService.uploadUserImage(image);
        var response = new OwnSuccessResponse(newName);
        log.info("Uploaded a new image, its new name: {}", newName);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/auth/tag")
    public ResponseEntity<OwnSuccessResponse> newTag(@Valid @RequestBody TagDto tagDto) {

        log.info("Creating a new tag: " + tagDto);
        userService.newTag(tagDto);
        var response = new OwnSuccessResponse("Added successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/auth/tag")
    public ResponseEntity<OwnSuccessResponse> updateTag(@Valid @RequestBody TagDto tagDto) {

        log.info("Updating a tag: " + tagDto);
        userService.updateTag(tagDto);
        var response = new OwnSuccessResponse("Updated successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/auth/tag/{id}")
    public ResponseEntity<OwnSuccessResponse> delTag(@PathVariable Long id) {

        log.info("Deleting a tag with ID: {}", id);
        userService.delTag(id);
        var response = new OwnSuccessResponse("Deleted successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
