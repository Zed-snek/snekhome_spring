package ed.back_snekhome.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
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

}
