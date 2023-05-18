package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.UserPrivateDto;
import ed.back_snekhome.dto.UserPublicDto;
import ed.back_snekhome.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

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




}
