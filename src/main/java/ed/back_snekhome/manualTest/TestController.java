package ed.back_snekhome.manualTest;

import ed.back_snekhome.entities.communityDemocracy.Candidate;
import ed.back_snekhome.manualTest.TestRepository;
import ed.back_snekhome.repositories.democracy.CandidateRepository;
import ed.back_snekhome.repositories.democracy.VoteRepository;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommunityMethodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestRepository testRepository;
    private final CommunityMethodsService communityMethodsService;

    @GetMapping("/api/test")
    @Transactional
    public ResponseEntity<OwnSuccessResponse> apiTest() {
        System.out.println("worked 1");


        var response = new OwnSuccessResponse("it worked: ");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
