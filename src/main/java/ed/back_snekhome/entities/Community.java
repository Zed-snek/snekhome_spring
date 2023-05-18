package ed.back_snekhome.entities;


import ed.back_snekhome.enums.CommunityType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "communities", uniqueConstraints = @UniqueConstraint(columnNames = {"id_name"} ) )
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Community {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCommunity;

    private String idName;
    private String name;


    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private CommunityType type;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account")
    private UserEntity owner;

    @OneToMany(mappedBy = "community", fetch = FetchType.EAGER)
    private List<CommunityRole> roles;

    private boolean isAnonymous; //allows or disallows to make anonymous posts in community
    private boolean isClosed; //community is closed, only users can invite

    //CommunityRoles<>;



}
