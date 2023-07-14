package ed.back_snekhome.dto.postDTOs;


import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NewPostDto {

    private String groupname;
    private String isAnonymous;
    private String text;
    private List<MultipartFile> images = new ArrayList<>();

}
