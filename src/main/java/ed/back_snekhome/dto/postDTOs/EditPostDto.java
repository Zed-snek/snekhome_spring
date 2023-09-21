package ed.back_snekhome.dto.postDTOs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class EditPostDto {

    private String text;
    private List<String> oldImages = new ArrayList<>();
    private List<MultipartFile> newImages = new ArrayList<>();

}
