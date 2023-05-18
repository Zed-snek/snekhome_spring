package ed.back_snekhome.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "info_tags" )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfoTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTag;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_account")
    @JsonIgnore
    private UserEntity user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String text;


}
