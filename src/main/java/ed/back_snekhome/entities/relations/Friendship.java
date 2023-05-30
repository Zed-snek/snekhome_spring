package ed.back_snekhome.entities.relations;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friendships")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    private Long idFirstUser;
    private Long idSecondUser;

    private boolean isFirstUser;
    private boolean isSecondUser;

}

