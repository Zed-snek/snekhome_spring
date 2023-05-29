package ed.back_snekhome.entities;


import ed.back_snekhome.entities.helpful.Image;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "userImages", uniqueConstraints = @UniqueConstraint(columnNames = {"name"} ) )
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserImage extends Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImage;

    private String name;

    @ManyToOne(cascade = CascadeType.ALL) //CascadeType.ALL = removes all images if user is removed
    @JoinColumn(name = "id_account")
    private UserEntity user;

}
