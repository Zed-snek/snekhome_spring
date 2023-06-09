package ed.back_snekhome.entities.post;

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
public class PostImage extends Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long idImage;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_post")
    @JsonIgnore
    private Post post;

}
