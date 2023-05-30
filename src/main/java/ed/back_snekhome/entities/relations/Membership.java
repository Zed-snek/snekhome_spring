package ed.back_snekhome.entities.relations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "memberships")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;



}
