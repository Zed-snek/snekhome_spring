package ed.back_snekhome.controllers;

import ed.back_snekhome.response.OwnSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class TestController {


    @GetMapping("/api/test")
    public ResponseEntity<OwnSuccessResponse> apiTest() {

        System.out.println("Worked");

        var response = new OwnSuccessResponse("it worked!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
