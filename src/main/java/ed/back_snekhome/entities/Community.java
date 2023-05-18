package ed.back_snekhome.entities;


import ed.back_snekhome.enums.CommunityType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "communities" )
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Community {


    @Id
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private CommunityType type;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account")
    private UserEntity owner;

    private boolean isAnonymous; //allows or disallows to make anonymous posts in community
    private boolean isClosed; //community is closed, only users can invite

    //CommunityRoles<>;



}
