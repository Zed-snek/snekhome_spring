package ed.back_snekhome.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.helpful.Image;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name"} ) )
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class CommunityImage extends Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImage;

    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_community")
    @JsonIgnore
    private Community community;

}
