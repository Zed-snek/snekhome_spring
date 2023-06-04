package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityCardDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityDto;
import ed.back_snekhome.dto.communityDTOs.UpdateCommunityDto;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommunityService;
import ed.back_snekhome.services.RelationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommunityController {

    private final CommunityService communityService;
    private final RelationsService relationsService;

    @GetMapping("/community/{name}")
    public PublicCommunityDto getCommunity(@PathVariable String name) {


        return communityService.getPublicCommunityDto(name);
    }

    @PostMapping("/auth/community")
    public ResponseEntity<OwnSuccessResponse> newCommunity(@RequestBody NewCommunityDto dto) {

        communityService.newCommunity(dto);

        var response = new OwnSuccessResponse("Community has been created");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/community")
    public ResponseEntity<OwnSuccessResponse> updateCommunity(@RequestBody UpdateCommunityDto dto) {
        communityService.updateCommunity(dto);
        var response = new OwnSuccessResponse("Community has been updated");
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @DeleteMapping("/auth/community/{name}")
    public ResponseEntity<OwnSuccessResponse> delCommunity(@PathVariable String name) {

        communityService.deleteCommunity(name);

        var response = new OwnSuccessResponse("Community has been deleted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/community/isNotTaken/{name}")
    public boolean isNotTaken(@PathVariable String name) {

        return communityService.isNameTaken(name);
    }

    @PostMapping("/auth/community/member/{name}")
    public ResponseEntity<OwnSuccessResponse> joinCommunity(@PathVariable String name) {
        relationsService.joinCommunity(name);
        var response = new OwnSuccessResponse("User has joined community");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/auth/community/member/{name}")
    public ResponseEntity<OwnSuccessResponse> leaveCommunity(@PathVariable String name) {
        relationsService.leaveCommunity(name);
        var response = new OwnSuccessResponse("User has left community");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/auth/communities/home_cards")
    public ArrayList<PublicCommunityCardDto> getHomeCards() { //

        return communityService.getHomeCards();
    }

}
