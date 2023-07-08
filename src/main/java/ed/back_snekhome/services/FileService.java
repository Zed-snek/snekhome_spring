package ed.back_snekhome.services;

import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.post.PostImage;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.CommunityImageRepository;
import ed.back_snekhome.repositories.PostImageRepository;
import ed.back_snekhome.repositories.UserImageRepository;
import ed.back_snekhome.utils.GenerationFunctions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${upload.path}")
    private String uploadPath;

    final private CommunityImageRepository communityImageRepository;
    final private UserImageRepository userImageRepository;
    final private UserMethodsService userMethodsService;
    final private RelationsService relationsService;
    final private PostImageRepository postImageRepository;

    public byte[] getImageByName(String imageName) throws IOException {

        Path destination;
        if (imageName.length() < 10) {
            destination = Paths.get(uploadPath + "systemImages/" + imageName);
        }
        else {
            destination = Paths.get(uploadPath + imageName);
        }

        return IOUtils.toByteArray(destination.toUri());
    }

    public String deleteImageByName(String name) {
        var userImage = userImageRepository.findByName(name);
        if (userImage.isPresent()) {
            if (!userMethodsService.isCurrentUserEqual(userImage.get().getUser()))
                throw new UnauthorizedException("No permissions to delete image");
            userImageRepository.delete(userImage.get());
            return "Image is deleted";
        }

        var communityImage = communityImageRepository.findByName(name);
        if (communityImage.isPresent()) {
            var membership = relationsService.getMembership(
                    userMethodsService.getCurrentUser(),
                    communityImage.get().getCommunity()
            );
            if (membership.getRole() == null || !membership.getRole().isEditDescription())
                throw new UnauthorizedException("No permissions to delete image");
            communityImageRepository.delete(communityImage.get());
            return "Image is deleted";
        }
        throw new EntityNotFoundException("Image is not found");
    }

    public String uploadImageNameReturned(MultipartFile file) throws IOException { //returns new file name

        String name = GenerationFunctions.generateCode(5) + System.currentTimeMillis();

        name += getFileExtension( file.getOriginalFilename() );

        file.transferTo( new File( uploadPath + name ) );

        return name;
    }


    public void uploadPostImages(List<MultipartFile> images, Post post) throws IOException {
        for (MultipartFile image : images) {
            var img = PostImage.builder()
                    .name(uploadImageNameReturned(image))
                    .post(post)
                    .build();
            postImageRepository.save(img);
        }
    }


    private String getFileExtension( String fileName ) {
        int lastDot = fileName.lastIndexOf('.');

        if ( lastDot == -1 ) //if dot was not found, method lastIndexOf returns value "-1"
            return "";
        else
            return fileName.substring( lastDot );
    }


}
