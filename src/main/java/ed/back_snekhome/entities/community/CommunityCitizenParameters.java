package ed.back_snekhome.entities.community;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@Table(name = "citizen_parameters")
@NoArgsConstructor
@AllArgsConstructor
public class CommunityCitizenParameters {

    @Id
    @Column(name = "id_community")
    @JsonIgnore
    private Long idCommunity;


    @OneToOne
    @MapsId
    @JoinColumn(name = "id_community")
    @JsonIgnore
    private Community community;


    private int days;
    private int rating;

    private int electionDays;

}
