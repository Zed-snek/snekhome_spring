package ed.back_snekhome.dto.postDTOs;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
public class EditPostDto {

    private String text;
    private List<String> oldImages = new ArrayList<>();
    private List<MultipartFile> newImages = new ArrayList<>();

}
