package ed.back_snekhome.dto.userDTOs;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TagDto {

    private Long id;

    @Size(min = 1, max = 50, message = "Title is required and should not exceed 50 symbols")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Size(min = 1, max = 1024, message = "Text is required and should not exceed 1024 symbols")
    private String text;

}
