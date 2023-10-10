package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.communityDTOs.GeneralDemocracyDto;
import ed.back_snekhome.dto.communityDTOs.NewCandidateDto;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.DemocracyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DemocracyController {

    private final DemocracyService democracyService;

    @GetMapping("/democracy/{name}")
    public GeneralDemocracyDto getCommunity(@PathVariable String name) {
        return democracyService.getGeneralDemocracyData(name);
    }

    @PostMapping("/democracy/vote/{groupname}")
    public ResponseEntity<OwnSuccessResponse> makeVote(
            @RequestParam(name = "nickname") String candidateNickname,
            @PathVariable String groupname
    ) {
        democracyService.makeVote(groupname, candidateNickname);

        var response = new OwnSuccessResponse("Vote is accepted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/democracy/candidate/{groupname}/activate")
    public ResponseEntity<OwnSuccessResponse> activateCandidate(@PathVariable String groupname) {
        democracyService.activateCandidate(groupname);

        var response = new OwnSuccessResponse("Candidate is activated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/democracy/candidate")
    public ResponseEntity<OwnSuccessResponse> newCandidate(@ModelAttribute NewCandidateDto dto) {

        democracyService.becomeCandidate(dto);

        var response = new OwnSuccessResponse("Candidate is created");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/democracy/candidate")
    public ResponseEntity<OwnSuccessResponse> updateCandidateProgram(@ModelAttribute NewCandidateDto dto) {

        democracyService.updateCandidateProgram(dto);

        var response = new OwnSuccessResponse("Candidate program is updated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
