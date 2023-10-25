package ed.back_snekhome.entities.communityDemocracy;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ElectionsParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_elections")
    private Elections elections;

    private int electionsNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_candidate")
    private Candidate candidate;

    @OneToMany(mappedBy = "electionsParticipation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Vote> votes;

    @Nullable
    private int numberOfVotes; //set when elections are finished

}
