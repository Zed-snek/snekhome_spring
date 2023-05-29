package ed.back_snekhome.controllers;

import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {


    private final UserService userService;

    @GetMapping("/api/test")
    public ResponseEntity<OwnSuccessResponse> apiTest() {

        System.out.println("Worked");
        var user = userService.getCurrentUser();
        System.out.println(user.getName() + " " + user.getNicknameColor());

        var response = new OwnSuccessResponse("it worked!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
