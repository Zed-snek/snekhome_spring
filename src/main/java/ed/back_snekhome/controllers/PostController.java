package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.postDTOs.NewPostDto;
import ed.back_snekhome.dto.postDTOs.PostDto;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.CommunityService;
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

    @PostMapping(value = "/auth/post", consumes = "multipart/form-data")
    public ResponseEntity<OwnSuccessResponse> newPost(@ModelAttribute NewPostDto dto) throws IOException {

        var response = new OwnSuccessResponse(postService.newPost(dto) + "");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/post/{id}")
    public PostDto getPostPage(@PathVariable Long id) {
        return postService.getPostPage(id);
    }

}
