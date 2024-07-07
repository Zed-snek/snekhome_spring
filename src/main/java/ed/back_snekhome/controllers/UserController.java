package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.userDTOs.UserPrivateDto;
import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.FriendshipService;
import ed.back_snekhome.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final FriendshipService friendshipService;

    @GetMapping("/auth/user/navbar")
    public UserPublicDto navbar() {

        log.info("Fetching navbar information");

        return userService.getNavbarInfo();
    }

    @GetMapping("/auth/user/current")
    public UserPrivateDto currentUser() {

        log.info("Fetching current user information");

        return userService.getCurrentUserInfo();
    }


    @GetMapping("/user/{name}")
    public UserPublicDto page(@PathVariable(value = "name") String nickname) {

        log.info("Fetching user information for nickname: {}", nickname);

        return userService.getUserInfo(nickname);
    }

    @PostMapping("/auth/friend/{nickname}")
    public ResponseEntity<OwnSuccessResponse> addFriend(@PathVariable String nickname) {

        log.info("Adding friend with nickname: {}", nickname);
        friendshipService.addFriend(nickname);

        var response = new OwnSuccessResponse("Request is sent successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/auth/friend/{nickname}")
    public ResponseEntity<OwnSuccessResponse> delFriend(@PathVariable String nickname) {

        log.info("Deleting friend with nickname: {}", nickname);
        friendshipService.delFriend(nickname);

        var response = new OwnSuccessResponse("Friend has deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user/friends/{nickname}")
    public List<UserPublicDto> getFriends(@PathVariable String nickname) {

        log.info("Fetching friends for user with nickname: {}", nickname);

        return friendshipService.getFriends(nickname);
    }




}
