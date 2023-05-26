package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.userDTOs.*;
import ed.back_snekhome.response.AuthenticationResponse;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserManagementController {


    private final UserService userService;


    @PostMapping("/user/login")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@RequestBody LoginDto loginDto) {

        AuthenticationResponse response = userService.loginUser(loginDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/user/register")
    public ResponseEntity<OwnSuccessResponse> registerUser(@RequestBody RegisterDto registerDto){

        userService.saveNewAccount(registerDto);

        var response = new OwnSuccessResponse("Confirm registration on email");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/user/confirmation")
    public ResponseEntity<OwnSuccessResponse> confirmationManager(@RequestParam String token) {

        var response = new OwnSuccessResponse( userService.confirmToken(token) ); //method confirmToken() returns a message

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/auth/user/password")
    public ResponseEntity<OwnSuccessResponse> updatePassword(@RequestBody ChangePasswordDto dto) {

        userService.changePassword(dto);
        var response = new OwnSuccessResponse("Password has been changed");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/user/email")
    public ResponseEntity<OwnSuccessResponse> updateEmail(@RequestBody ChangeEmailDto dto) {


        userService.changeEmail(dto.getEmail());

        var response = new OwnSuccessResponse("Check your email address to change to confirm action");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/auth/user/current")
    public ResponseEntity<OwnSuccessResponse> updateUser(@RequestBody UserUpdateDto dto) {

        userService.updateUser(dto);
        var response = new OwnSuccessResponse("User has been updated");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PostMapping(path = "/auth/user/current/image", consumes = "multipart/form-data")
    public ResponseEntity<OwnSuccessResponse> newImage(@RequestParam("image") MultipartFile image) throws IOException {


        var response = userService.uploadUserImage(image);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/auth/tag")
    public ResponseEntity<OwnSuccessResponse> newTag(@RequestBody TagDto tagDto) {

        userService.newTag(tagDto);
        var response = new OwnSuccessResponse("Added successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/tag")
    public ResponseEntity<OwnSuccessResponse> updateTag(@RequestBody TagDto tagDto) {

        userService.updateTag(tagDto);
        var response = new OwnSuccessResponse("Updated successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/auth/tag/{id}")
    public ResponseEntity<OwnSuccessResponse> delTag(@PathVariable Long id) {

        userService.delTag(id);
        var response = new OwnSuccessResponse("Deleted successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
