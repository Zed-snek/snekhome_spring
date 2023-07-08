package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.postDTOs.NewPostDto;
import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    @PostMapping(value = "/auth/post", consumes = "multipart/form-data")
    public ResponseEntity<OwnSuccessResponse> newPost(@ModelAttribute NewPostDto dto) throws IOException {

        postService.newPost(dto);

        var response = new OwnSuccessResponse("Post is created");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
