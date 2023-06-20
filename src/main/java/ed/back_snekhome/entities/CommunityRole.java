package ed.back_snekhome.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.relations.Membership;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class CommunityRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long idRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_community")
    @JsonIgnore
    private Community community;

    private String title;
    private String bannerColor; //HEX
    private String textColor; //HEX || "black"/"blue"/.. from CSS

    //moderation permissions:
    private boolean editId;
    private boolean editDescription; //image, name, description
    private boolean deletePosts;
    private boolean banUser;
    private boolean banCitizen; //if democracy

    private boolean isCitizen; //for democracy communities: vote and becoming president
    private boolean isCreator;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Membership> memberships;

}
