package ed.back_snekhome.dto.userDTOs;


import ed.back_snekhome.entities.user.InfoTag;
import ed.back_snekhome.security.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserPrivateDto {

    private Long idAccount;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String name;
    private String surname;

    private List<InfoTag> tags;

}
