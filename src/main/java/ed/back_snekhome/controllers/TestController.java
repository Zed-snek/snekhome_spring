package ed.back_snekhome.controllers;

import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.repositories.community.MembershipRepository;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.DemocracyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class TestController {

    private final MembershipRepository membershipRepository;
    private final DemocracyService democracyService;

    @GetMapping("/api/test")
    public ResponseEntity<OwnSuccessResponse> apiTest() {

        System.out.println("Worked");

        var response = new OwnSuccessResponse("it worked! ");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
