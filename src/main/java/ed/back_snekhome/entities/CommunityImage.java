package ed.back_snekhome.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name"} ) )
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class CommunityImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImage;

    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_community")
    private Community community;

}
