package ed.back_snekhome.services;

import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.post.PostImage;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.FileCantDeleteException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.community.CommunityImageRepository;
import ed.back_snekhome.repositories.post.PostImageRepository;
import ed.back_snekhome.repositories.user.UserImageRepository;
import ed.back_snekhome.utils.MyFunctions;
import ed.back_snekhome.helperComponents.MembershipHelper;
import ed.back_snekhome.helperComponents.UserHelper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${upload.path}")
    private String uploadPath;

    private final MembershipHelper membershipHelper;
    private final UserHelper userHelper;
    private final CommunityLogService communityLogService;
    private final PostImageRepository postImageRepository;
    private final CommunityImageRepository communityImageRepository;
    private final UserImageRepository userImageRepository;


    @Transactional
    @SneakyThrows
    public String deleteCommunityOrUserImageByName(String name) {
        var userImage = userImageRepository.findByName(name);
        if (userImage.isPresent()) {
            if (!userHelper.isCurrentUserEqual(userImage.get().getUser()))
                throw new UnauthorizedException("No permissions to delete image");
            userImageRepository.delete(userImage.get());
            deleteImageFromStorage(name);
            return "Image is deleted";
        }

        var communityImage = communityImageRepository.findByName(name);
        if (communityImage.isPresent()) {
            var membership = membershipHelper.getMembershipOrThrowErr(
                    userHelper.getCurrentUser(),
                    communityImage.get().getCommunity()
            );
            if (membership.getRole() == null || !membership.getRole().isEditDescription())
                throw new UnauthorizedException("No permissions to delete image");
            communityLogService.createLogUpdateImage(communityImage.get().getCommunity(), true);
            communityImageRepository.delete(communityImage.get());
            deleteImageFromStorage(name);
            return "Image is deleted";
        }

        throw new EntityNotFoundException("Image is not found");
    }

    @Transactional
    @SneakyThrows
    public void deletePostImageByName(String name) {
        var postImage = postImageRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Image is not found"));
        postImageRepository.delete(postImage);
        deleteImageFromStorage(name);
    }

    public void uploadPostImages(List<MultipartFile> images, Post post) {
        for (MultipartFile image : images) {
            var img = PostImage.builder()
                    .name(uploadImageNameReturned(image))
                    .post(post)
                    .build();
            postImageRepository.save(img);
        }
    }

    //Universal methods:
    @SneakyThrows
    public byte[] getImageByName(String imageName) {

        Path destination;
        if (imageName.length() < 10) {
            destination = Paths.get(uploadPath + "systemImages/" + imageName);
        }
        else {
            destination = Paths.get(uploadPath + imageName);
        }

        return IOUtils.toByteArray(destination.toUri());
    }

    @SneakyThrows
    public String uploadImageNameReturned(MultipartFile file) { //returns new file name

        String name = MyFunctions.generateCode(5) + System.currentTimeMillis();

        name += getFileExtension(file.getOriginalFilename());

        file.transferTo(new File(uploadPath + name));

        return name;
    }

    @SneakyThrows
    public void deleteImageFromStorage(String fileName) {
        File file = new File(uploadPath + fileName);
        if (file.exists()) {
            if (!file.delete()) {
                throw new FileCantDeleteException();
            }
        }
        else {
            throw new FileNotFoundException();
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');

        if (lastDot == -1) //if dot was not found, method lastIndexOf returns value "-1"
            return "";
        else
            return fileName.substring( lastDot );
    }

}
