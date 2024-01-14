package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.communityDTOs.CandidateListDto;
import ed.back_snekhome.dto.communityDTOs.GeneralDemocracyDto;
import ed.back_snekhome.dto.communityDTOs.NewCandidateDto;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.DemocracyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DemocracyController {

    private final DemocracyService democracyService;

    @GetMapping("/democracy/{name}")
    public GeneralDemocracyDto getCommunity(@PathVariable String name) {

        log.info("Fetching democracy data for community with groupname: {}", name);
        return democracyService.getGeneralDemocracyData(name);
    }

    @PostMapping("/auth/democracy/vote/{groupname}")
    public ResponseEntity<OwnSuccessResponse> makeVote(
            @PathVariable String groupname,
            @RequestParam(name = "nickname") String candidateNickname
    ) {

        log.info("Making a vote in community with groupname: {}, for candidate with nickname: {}", groupname, candidateNickname);
        democracyService.makeVote(groupname, candidateNickname);

        var response = new OwnSuccessResponse("Vote is accepted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/democracy/candidate/{groupname}/activate")
    public ResponseEntity<OwnSuccessResponse> activateCandidate(@PathVariable String groupname) {

        log.info("User requested for activating his candidacy in community with groupname: {}", groupname);
        democracyService.activateCandidate(groupname);

        var response = new OwnSuccessResponse("Candidate is activated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/democracy/candidate")
    public ResponseEntity<OwnSuccessResponse> newCandidate(@RequestBody NewCandidateDto dto) {

        log.info("Becoming a candidate: {}", dto);
        democracyService.becomeCandidate(dto);

        var response = new OwnSuccessResponse("Candidate is created");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/democracy/candidate")
    public ResponseEntity<OwnSuccessResponse> updateCandidateProgram(@RequestBody NewCandidateDto dto) {

        log.info("Updating candidate program: {}", dto);
        democracyService.updateCandidateProgram(dto);

        var response = new OwnSuccessResponse("Candidate program is updated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/democracy/candidates/{groupname}")
    public CandidateListDto getCandidateList(@PathVariable String groupname) {

        log.info("Fetching current candidate list in community with groupname: {}", groupname);
        return democracyService.getListOfCandidates(groupname);
    }

}
