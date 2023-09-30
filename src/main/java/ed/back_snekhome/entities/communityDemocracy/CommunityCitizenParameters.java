package ed.back_snekhome.entities.communityDemocracy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.community.Community;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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


    @Min(value = 7, message = "Minimum to become citizen is 7 days")
    @Max(value = 365, message = "Maximum to become citizen is 365 days")
    private int days;

    @Min(value = 0, message = "Value can't be negative")
    @Max(value = 365, message = "Maximum to become citizen is 10000 rating")
    private int rating;

    @Min(value = 7, message = "Elections can't be more frequently than one per 7 days")
    @Max(value = 365, message = "Elections must be at least once per 365 days")
    private int electionDays;

}
