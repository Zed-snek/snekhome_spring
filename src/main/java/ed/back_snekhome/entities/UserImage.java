package ed.back_snekhome.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "userImages", uniqueConstraints = @UniqueConstraint(columnNames = {"name"} ) )
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImage;

    private String name;

    @ManyToOne(optional = false, cascade = CascadeType.ALL) //CascadeType.ALL = removes all images if user is removed
    @JoinColumn(name = "id_account")
    private UserEntity user;

}
