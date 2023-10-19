package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.communityDTOs.CandidateDto;
import ed.back_snekhome.dto.communityDTOs.GeneralDemocracyDto;
import ed.back_snekhome.dto.communityDTOs.NewCandidateDto;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.DemocracyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DemocracyController {

    private final DemocracyService democracyService;

    @GetMapping("/democracy/{name}")
    public GeneralDemocracyDto getCommunity(@PathVariable String name) {
        return democracyService.getGeneralDemocracyData(name);
    }

    @PostMapping("/auth/democracy/vote/{groupname}")
    public ResponseEntity<OwnSuccessResponse> makeVote(
            @RequestParam(name = "nickname") String candidateNickname,
            @PathVariable String groupname
    ) {
        democracyService.makeVote(groupname, candidateNickname);

        var response = new OwnSuccessResponse("Vote is accepted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/democracy/candidate/{groupname}/activate")
    public ResponseEntity<OwnSuccessResponse> activateCandidate(@PathVariable String groupname) {
        democracyService.activateCandidate(groupname);

        var response = new OwnSuccessResponse("Candidate is activated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/democracy/candidate")
    public ResponseEntity<OwnSuccessResponse> newCandidate(@RequestBody NewCandidateDto dto) {

        democracyService.becomeCandidate(dto);

        var response = new OwnSuccessResponse("Candidate is created");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/democracy/candidate")
    public ResponseEntity<OwnSuccessResponse> updateCandidateProgram(@RequestBody NewCandidateDto dto) {

        democracyService.updateCandidateProgram(dto);

        var response = new OwnSuccessResponse("Candidate program is updated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/democracy/candidates/{groupname}")
    public List<CandidateDto> getCandidateList(@PathVariable String groupname) {

        return democracyService.getListOfCandidates(groupname);
    }

}
