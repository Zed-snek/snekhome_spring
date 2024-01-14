package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.postDTOs.*;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommentaryService;
import ed.back_snekhome.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostController {

    private static final String DEFAULT_PAGE_SIZE = "10";

    private final PostService postService;
    private final CommentaryService commentaryService;

    @PostMapping(value = "/auth/post", consumes = "multipart/form-data")
    public ResponseEntity<OwnSuccessResponse> newPost(@ModelAttribute NewPostDto dto) throws IOException {

        log.info("Creating a new post");
        var response = new OwnSuccessResponse(postService.newPost(dto) + "");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping(value = "/auth/post/{id}", consumes = "multipart/form-data")
    public ResponseEntity<OwnSuccessResponse> updatePost(
            @ModelAttribute EditPostDto dto,
            @PathVariable Long id
    ) throws IOException {

        log.info("Updating a post with ID: {}", id);
        postService.updatePost(dto, id);

        var response = new OwnSuccessResponse("Post has been updated successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/auth/post/{id}")
    public ResponseEntity<OwnSuccessResponse> deletePost(@PathVariable Long id) throws FileNotFoundException {

        log.info("Deleting a post with ID: {}", id);
        postService.deletePost(id);

        var response = new OwnSuccessResponse("Post has been deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/post/{id}")
    public PostDto getPostPage(@PathVariable Long id) {

        log.info("Fetching a post with ID: {}", id);
        return postService.getPostPage(id);
    }


    @PostMapping("/auth/post/{id}/rate/{newStatus}")
    public ResponseEntity<OwnSuccessResponse> ratePost(@PathVariable Long id, @PathVariable RatingType newStatus) {

        log.info("Rating a post with ID: {}", id);
        postService.ratePost(id, newStatus);

        var response = new OwnSuccessResponse("Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/auth/commentary/{id}/rate/{newStatus}")
    public ResponseEntity<OwnSuccessResponse> rateComment(@PathVariable Long id, @PathVariable RatingType newStatus) {

        log.info("Rating a commentary with ID: {}", id);
        commentaryService.rateComment(id, newStatus);

        var response = new OwnSuccessResponse("Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/auth/post/{id}/commentary")
    public ResponseEntity<OwnSuccessResponse> newComment(@PathVariable Long id,
                                                         @Valid @RequestBody NewCommentaryDto dto
    ) {

        log.info("Creating a commentary in post with ID: {}", id);

        var response = new OwnSuccessResponse(commentaryService.newComment(id, dto).toString());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/post/{id}/commentaries")
    public List<CommentaryDto> getComments(@PathVariable Long id) {

        log.info("Fetching commentaries by post with ID: {}", id);

        return commentaryService.getCommentariesByPostId(id);
    }


    @PutMapping("/auth/commentary/{id}")
    public ResponseEntity<OwnSuccessResponse> updateComment(@PathVariable Long id,
                                                            @Valid @RequestBody NewCommentaryDto dto
    ) {

        log.info("Updating commentary with ID: {}", id);
        commentaryService.updateCommentary(dto.getText(), id);

        var response = new OwnSuccessResponse("Commentary has been updated successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/auth/commentary/{id}")
    public ResponseEntity<OwnSuccessResponse> deleteComment(@PathVariable Long id) {

        log.info("Deleting commentary with ID: {}", id);
        commentaryService.deleteComment(id);

        var response = new OwnSuccessResponse("Commentary has been deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/user/posts/{nickname}")
    public List<PostDto> getPostsUser(
            @PathVariable String nickname,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize
    ) {

        log.info("Received request for fetching posts for user with nickname: {}", nickname);
        return postService.getPostDtoListByUser(nickname, page, pageSize);
    }


    @GetMapping("/community/posts/{groupname}")
    public List<PostDto> getPostsCommunity(
            @PathVariable String groupname,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = "NEW", required = false) String type
    ) {

        log.info("Fetching posts for community with groupname: {}", groupname);
        return postService.getPostDtoListByCommunity(groupname, page, pageSize, type);
    }


    @GetMapping("/auth/user/posts/home")
    public List<PostDto> getPostsHome(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize
    ) {

        log.info("Fetching posts for home page");
        return postService.getPostDtoListHome(page, pageSize);
    }


}
