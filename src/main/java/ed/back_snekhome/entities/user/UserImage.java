package ed.back_snekhome.entities.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.helpful.Image;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name"} ) )
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserImage extends Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long idImage;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account")
    @JsonIgnore
    @CreatedBy
    private UserEntity user;

}
