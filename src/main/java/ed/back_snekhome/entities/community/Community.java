package ed.back_snekhome.entities.community;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.enums.CommunityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"groupname"} ) )
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Community {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long idCommunity;

    private String groupname; //id
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "community", fetch = FetchType.LAZY, cascade = CascadeType.ALL) //LAZY = loads only when is required. EAGER = loads all elements
    private List<CommunityImage> images;

    private LocalDate creation;

    @Enumerated(EnumType.STRING)
    private CommunityType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account")
    @JsonIgnore
    private UserEntity owner;

    @OneToMany(mappedBy = "community", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CommunityRole> roles;

    private boolean anonAllowed; //allows or disallows to make anonymous posts in community
    private boolean isClosed; //community is closed, only users can invite
    private boolean isInviteUsers; //for closed communities, sets if anyone can invite users, otherwise will be able only the ranked ones

    @OneToMany(mappedBy = "community", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Membership> memberships;

    //for democracy, citizen requirements:
    @OneToOne(mappedBy = "community", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private CommunityCitizenParameters citizenParameters;

}
