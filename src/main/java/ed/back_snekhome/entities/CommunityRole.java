package ed.back_snekhome.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommunityRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_community")
    private Community community;

    private String title;
    private String bannerColor; //HEX


    //moderation permissions:
    private boolean editId;
    private boolean editDescription; //image, name, description
    private boolean deletePosts;
    private boolean banUser;
    private boolean banCitizen; //if democracy

    //other permissions
    private boolean makePosts;
    private boolean inviteUsers; //for closed communities only


    private boolean isCitizen; //for democracy communities: vote and becoming president

}
