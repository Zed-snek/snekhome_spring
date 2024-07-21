package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.communityDTOs.*;
import ed.back_snekhome.dto.userDTOs.UserPublicDto;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommunityLogService;
import ed.back_snekhome.services.CommunityService;
import ed.back_snekhome.services.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommunityController {

    private final CommunityService communityService;
    private final MembershipService membershipService;
    private final CommunityLogService communityLogService;

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
        membershipService.joinCommunity(name);
        var response = new OwnSuccessResponse("User has joined community");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/auth/community/member/{name}")
    public ResponseEntity<OwnSuccessResponse> leaveCommunity(@PathVariable String name) {
        membershipService.leaveCommunity(name);
        var response = new OwnSuccessResponse("User has left community");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/auth/communities/home_cards")
    public List<PublicCommunityCardDto> getHomeCards() {

        return communityService.getHomeCards();
    }

    @PostMapping(path = "/auth/community/image/{groupname}", consumes = "multipart/form-data")
    @SneakyThrows
    public ResponseEntity<OwnSuccessResponse> newImage(
            @RequestParam("image") MultipartFile image,
            @PathVariable String groupname
    ) {

        var response = communityService.uploadCommunityImage(image, groupname);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/community_list/{nickname}")
    public List<PublicCommunityCardDto> getCommunityList(@PathVariable String nickname) {
        return membershipService.getJoinedCommunitiesByNickname(nickname);
    }


    @GetMapping("/members/{groupname}")
    public MembersDto getMembers(@PathVariable String groupname) {
        return membershipService.getMembersByCommunity(groupname, false);
    }


    @GetMapping("/banned_users/{groupname}")
    public MembersDto getBannedUsers(@PathVariable String groupname) {
        return membershipService.getMembersByCommunity(groupname, true);
    }


    @PostMapping("/auth/community/role/{groupname}")
    public ResponseEntity<OwnSuccessResponse> newRole(@Valid @RequestBody CommunityRoleDto dto,
                                                      @PathVariable String groupname
    ) {
        communityService.newRole(dto, groupname);
        var response = new OwnSuccessResponse("Role is created");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/auth/community/role/{groupname}/{oldRoleName}")
    public ResponseEntity<OwnSuccessResponse> updateRole(
            @Valid @RequestBody CommunityRoleDto dto,
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
    public List<CommunityRole> getRoles(@PathVariable String groupname) {
        return communityService.getRoles(groupname);
    }


    @PostMapping("/auth/community/{groupname}/ban/{username}")
    public ResponseEntity<OwnSuccessResponse> banUser(
            @PathVariable String groupname,
            @PathVariable String username
    ) {
        membershipService.banUser(groupname, username);
        var response = new OwnSuccessResponse("User is banned");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/auth/community/{groupname}/unban/{username}")
    public ResponseEntity<OwnSuccessResponse> unbanUser(
            @PathVariable String groupname,
            @PathVariable String username
    ) {
        membershipService.unbanUser(groupname, username);
        var response = new OwnSuccessResponse("User is unbanned");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/auth/community/{groupname}/role/{roleName}/set/{username}")
    public ResponseEntity<OwnSuccessResponse> grantRole(
            @PathVariable String groupname,
            @PathVariable String username,
            @PathVariable String roleName
    ) {
        membershipService.grantRole(username, groupname, roleName);
        var response = new OwnSuccessResponse("Role is granted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/auth/community/{groupname}/role/revoke/{username}")
    public ResponseEntity<OwnSuccessResponse> revokeRole(
            @PathVariable String groupname,
            @PathVariable String username
    ) {
        membershipService.revokeRole(username, groupname);
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
        var response = new OwnSuccessResponse(membershipService.manageJoinRequest(groupname));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/auth/community/{groupname}/request")
    public List<UserPublicDto> getJoinRequests(@PathVariable String groupname) {

        return membershipService.getAllJoinRequests(groupname);
    }


    @PostMapping("/auth/community/{groupname}/request/{nickname}")
    public ResponseEntity<OwnSuccessResponse> acceptJoinRequest(
            @PathVariable String groupname, @PathVariable String nickname
    ) {
        membershipService.acceptJoinRequest(groupname, nickname);
        var response = new OwnSuccessResponse("Request is accepted, user is a member now");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/auth/community/{groupname}/request/{nickname}")
    public ResponseEntity<OwnSuccessResponse> cancelJoinRequest(
            @PathVariable String groupname, @PathVariable String nickname
    ) {
        membershipService.cancelJoinRequest(groupname, nickname);
        var response = new OwnSuccessResponse("Join request is cancelled");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/auth/community/{groupname}/logs")
    public List<CommunityLogDto> getCommunityLogs(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "30", required = false) int pageSize,
            @PathVariable String groupname
    ) {
        return communityLogService.getLogsByGroupname(groupname, page, pageSize);
    }

}
