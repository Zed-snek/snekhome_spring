package ed.back_snekhome.controllers;

import ed.back_snekhome.response.OwnSuccessResponse;
import ed.back_snekhome.services.FileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;


    @GetMapping("/image/{name}")
    @SneakyThrows
    public byte[] getImageByName(@PathVariable(value = "name") String name) {

        return fileService.getImageByName(name);
    }

    @DeleteMapping("/auth/image/{name}/delete")
    @SneakyThrows
    public ResponseEntity<OwnSuccessResponse> deleteCommunityOrUserImage(@PathVariable String name) {
        var response = new OwnSuccessResponse(fileService.deleteCommunityOrUserImageByName(name));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
