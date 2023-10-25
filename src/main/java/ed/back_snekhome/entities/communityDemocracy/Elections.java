package ed.back_snekhome.entities.communityDemocracy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.community.Community;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Elections {

    @Id
    @Column(name = "id_community")
    @JsonIgnore
    private Long idCommunity;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_community")
    @JsonIgnore
    private Community community;

    private LocalDate startDate;
    private LocalDate endDate;

    private int electionsNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_current_president")
    private Candidate currentPresident;


    public ElectionsParticipation createElectionsParticipation(Candidate candidate) {
        return ElectionsParticipation.builder()
                .elections(this)
                .electionsNumber(this.electionsNumber)
                .candidate(candidate)
                .build();
    }

}
