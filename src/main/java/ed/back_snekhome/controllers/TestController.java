package ed.back_snekhome.controllers;


import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.email.ConfirmationToken;
import ed.back_snekhome.email.ConfirmationTokenService;
import ed.back_snekhome.email.ConfirmationType;
import ed.back_snekhome.email.EmailSendService;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.repositories.UserRepository;
import ed.back_snekhome.response.OwnSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {



    @GetMapping("/api/test")
    public ResponseEntity<OwnSuccessResponse> updateUser(@RequestBody NewCommunityDto dto) {

        System.out.println("Worked");

        System.out.println(dto.getType());


        var response = new OwnSuccessResponse("it worked!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
