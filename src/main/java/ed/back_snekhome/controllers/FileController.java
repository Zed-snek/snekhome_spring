package ed.back_snekhome.controllers;

import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;


    @GetMapping("/image/{name}")
    public byte[] getImageByName(@PathVariable(value = "name") String name) throws IOException {

        return fileService.getImageByName(name);
    }

    @DeleteMapping("/auth/image/{name}/delete")
    public ResponseEntity<OwnSuccessResponse> deleteCommunityOrUserImage(@PathVariable String name) throws FileNotFoundException {
        var response = new OwnSuccessResponse(fileService.deleteCommunityOrUserImageByName(name));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
