package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.postDTOs.*;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommentaryService;
import ed.back_snekhome.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostController {

    private final PostService postService;
    private final CommentaryService commentaryService;

    @PostMapping(value = "/auth/post", consumes = "multipart/form-data")
    public ResponseEntity<OwnSuccessResponse> newPost(@ModelAttribute NewPostDto dto) throws IOException {

        var response = new OwnSuccessResponse(postService.newPost(dto) + "");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "/auth/post/{id}", consumes = "multipart/form-data")
    public ResponseEntity<OwnSuccessResponse> updatePost(
            @ModelAttribute EditPostDto dto,
            @PathVariable Long id
    ) throws IOException {

        postService.updatePost(dto, id);

        var response = new OwnSuccessResponse("Post has been updated successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/auth/post/{id}")
    public ResponseEntity<OwnSuccessResponse> deletePost(@PathVariable Long id) throws FileNotFoundException {
        postService.deletePost(id);

        var response = new OwnSuccessResponse("Post has been deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/post/{id}")
    public PostDto getPostPage(@PathVariable Long id) {
        return postService.getPostPage(id);
    }

    @PostMapping("/auth/post/{id}/rate/{newStatus}")
    public ResponseEntity<OwnSuccessResponse> ratePost(@PathVariable Long id, @PathVariable RatingType newStatus) {
        postService.ratePost(id, newStatus);
        var response = new OwnSuccessResponse("Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/commentary/{id}/rate/{newStatus}")
    public ResponseEntity<OwnSuccessResponse> rateComment(@PathVariable Long id, @PathVariable RatingType newStatus) {
        commentaryService.rateComment(id, newStatus);
        var response = new OwnSuccessResponse("Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/post/{id}/commentary")
    public ResponseEntity<OwnSuccessResponse> newComment(@PathVariable Long id, @RequestBody NewCommentaryDto dto) {
        var response = new OwnSuccessResponse(commentaryService.newComment(id, dto).toString());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/post/{id}/commentaries")
    public ArrayList<CommentaryDto> getComments(@PathVariable Long id) {
        return commentaryService.getCommentariesByPostId(id);
    }

    @PutMapping("/auth/commentary/{id}")
    public ResponseEntity<OwnSuccessResponse> updateComment(@PathVariable Long id, @RequestBody NewCommentaryDto dto) {
        commentaryService.updateCommentary(dto.getText(), id);

        var response = new OwnSuccessResponse("Commentary has been updated successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/auth/commentary/{id}")
    public ResponseEntity<OwnSuccessResponse> deleteComment(@PathVariable Long id) {
        commentaryService.deleteComment(id);

        var response = new OwnSuccessResponse("Commentary has been deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user/posts/{nickname}")
    public ArrayList<PostDto> getPostsUser(@PathVariable String nickname) {
        return postService.getPostDtoListByUser(nickname);
    }
    @GetMapping("/community/posts/{groupname}")
    public ArrayList<PostDto> getPostsCommunity(@PathVariable String groupname) {
        return postService.getPostDtoListByCommunity(groupname);
    }
    @GetMapping("/auth/user/posts/home")
    public ArrayList<PostDto> getPostsHome(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int pageSize
    ) {
        return postService.getPostDtoListHome(page, pageSize);
    }


}
