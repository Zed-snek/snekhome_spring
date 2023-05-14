package ed.back_snekhome.controllers;


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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final ConfirmationTokenService confirmationTokenService;


    @GetMapping("/api/auth/test")
    public ResponseEntity<OwnSuccessResponse> updateUser() {

        System.out.println("Worked");

        var user = userRepository.findByEmail( SecurityContextHolder.getContext().getAuthentication().getName() )
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));

        var confirmationToken = new ConfirmationToken(
                user.getIdAccount(),
                ConfirmationType.CHANGE_EMAIL,
                15, 45,
                "ed.savchuker@gmail.com");
        confirmationTokenService.save(confirmationToken);


        var response = new OwnSuccessResponse("it worked!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
}
