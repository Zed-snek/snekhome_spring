package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityDto;
import ed.back_snekhome.entities.Community;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommunityController {

    private final CommunityService communityService;

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

    @GetMapping("/community/isNotTaken/{name}")
    public boolean isNotTaken(@PathVariable String name) {

        return communityService.isNameTaken(name);
    }


}
