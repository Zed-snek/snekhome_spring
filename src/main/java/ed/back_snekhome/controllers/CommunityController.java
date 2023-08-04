package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.communityDTOs.CommunityRoleDto;
import ed.back_snekhome.dto.communityDTOs.MembersDto;
import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityCardDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityDto;
import ed.back_snekhome.dto.communityDTOs.UpdateCommunityDto;
import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommunityService;
import ed.back_snekhome.services.RelationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ArrayList<PublicCommunityCardDto> getHomeCards() {

        return communityService.getHomeCards();
    }

    @PostMapping(path = "/auth/community/image/{groupname}", consumes = "multipart/form-data")
    public ResponseEntity<OwnSuccessResponse> newImage(
            @RequestParam("image") MultipartFile image,
            @PathVariable String groupname
    ) throws IOException {

        var response = communityService.uploadCommunityImage(image, groupname);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/community_list/{nickname}")
    public ArrayList<PublicCommunityCardDto> getCommunityList(@PathVariable String nickname) {
        return relationsService.getJoinedCommunitiesByNickname(nickname);
    }

    @GetMapping("/members/{groupname}")
    public MembersDto getMembers(@PathVariable String groupname) {
        return relationsService.getMembersByCommunity(groupname);
    }

    @PostMapping("/auth/community/role/{groupname}")
    public ResponseEntity<OwnSuccessResponse> newRole(@RequestBody CommunityRoleDto dto, @PathVariable String groupname) {
        communityService.newRole(dto, groupname);
        var response = new OwnSuccessResponse("Role is created");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/community/role/{groupname}/{oldRoleName}")
    public ResponseEntity<OwnSuccessResponse> updateRole(
            @RequestBody CommunityRoleDto dto,
            @PathVariable String groupname,
            @PathVariable String oldRoleName
    ) {
        communityService.updateRole(dto, groupname, oldRoleName);
        var response = new OwnSuccessResponse("Role is updated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/auth/community/role/{groupname}/{roleName}")
    public ResponseEntity<OwnSuccessResponse> deleteRole(
            @PathVariable String groupname,
            @PathVariable String roleName
    ) {
        communityService.deleteRole(groupname, roleName);
        var response = new OwnSuccessResponse("Role is deleted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/community/roles/{groupname}")
    public Iterable<CommunityRole> getRoles(@PathVariable String groupname) {
        return communityService.getRoles(groupname);
    }

    @PostMapping("/auth/community/{groupname}/ban/{username}")
    public ResponseEntity<OwnSuccessResponse> banUser(
            @PathVariable String groupname,
            @PathVariable String username
    ) {
        relationsService.banUser(groupname, username);
        var response = new OwnSuccessResponse("User is banned");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/community/{groupname}/role/{roleName}/set/{username}")
    public ResponseEntity<OwnSuccessResponse> grantRole(
            @PathVariable String groupname,
            @PathVariable String username,
            @PathVariable String roleName
    ) {
        relationsService.grantRole(username, groupname, roleName);
        var response = new OwnSuccessResponse("Role is granted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/auth/community/{groupname}/role/revoke/{username}")
    public ResponseEntity<OwnSuccessResponse> revokeRole(
            @PathVariable String groupname,
            @PathVariable String username
    ) {
        relationsService.revokeRole(username, groupname);
        var response = new OwnSuccessResponse("Role is revoked");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/community/{groupname}/settings")
    public ResponseEntity<OwnSuccessResponse> updateCommunitySettings(
            @PathVariable String groupname,
            @RequestBody NewCommunityDto dto
    ) {
        communityService.updateCommunitySettings(groupname, dto);
        var response = new OwnSuccessResponse("Community settings are updated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/auth/community/{groupname}/democracy")
    public ResponseEntity<OwnSuccessResponse> updateDemocracySettings(
            @PathVariable String groupname,
            @RequestBody NewCommunityDto dto
    ) {
        communityService.updateCommunityDemocracySettings(groupname, dto);
        var response = new OwnSuccessResponse("Democracy settings are updated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/community/{groupname}/request")
    public ResponseEntity<OwnSuccessResponse> sendJoinRequest(@PathVariable String groupname) {
        var response = new OwnSuccessResponse(communityService.manageJoinRequest(groupname));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/auth/community/{groupname}/request")
    public ArrayList<UserPublicDto> getJoinRequests(@PathVariable String groupname) {

        return communityService.getAllJoinRequests(groupname);
    }

}
