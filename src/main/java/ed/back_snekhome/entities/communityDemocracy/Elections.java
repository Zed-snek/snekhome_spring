package ed.back_snekhome.entities.communityDemocracy;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Elections {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "elections", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Candidate> candidates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_current_president")
    private Candidate currentPresident;

}
