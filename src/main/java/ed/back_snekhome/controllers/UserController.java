package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.userDTOs.UserPrivateDto;
import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.RelationsService;
import ed.back_snekhome.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final RelationsService relationsService;

    @GetMapping("/auth/user/navbar")
    public UserPublicDto navbar() {

        return userService.getNavbarInfo();
    }

    @GetMapping("/auth/user/current")
    public UserPrivateDto currentUser() {

        return userService.getCurrentUserInfo();
    }


    @GetMapping("/user/{name}")
    public UserPublicDto page(@PathVariable(value = "name") String nickname) {

        return userService.getUserInfo(nickname);
    }

    @PostMapping("/auth/friend/{nickname}")
    public ResponseEntity<OwnSuccessResponse> addFriend(@PathVariable String nickname) {

        relationsService.addFriend(nickname);

        var response = new OwnSuccessResponse("Request is sent successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/auth/friend/{nickname}")
    public ResponseEntity<OwnSuccessResponse> delFriend(@PathVariable String nickname) {

        relationsService.delFriend(nickname);

        var response = new OwnSuccessResponse("Friend has deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user/friends/{nickname}")
    public ArrayList<UserPublicDto> getFriends(@PathVariable String nickname) {
        return relationsService.getFriends(nickname);
    }




}
