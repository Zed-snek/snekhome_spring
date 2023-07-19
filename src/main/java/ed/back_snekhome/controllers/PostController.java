package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.postDTOs.NewCommentaryDto;
import ed.back_snekhome.dto.postDTOs.NewPostDto;
import ed.back_snekhome.dto.postDTOs.PostDto;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommentaryService;
import ed.back_snekhome.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
        commentaryService.newComment(id, dto);
        var response = new OwnSuccessResponse("Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
