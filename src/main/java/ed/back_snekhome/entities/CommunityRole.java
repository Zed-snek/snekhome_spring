package ed.back_snekhome.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
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

}
