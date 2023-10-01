package ed.back_snekhome.entities.communityDemocracy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.community.Community;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresidencyData {

    @Id
    @Column(name = "id_community")
    @JsonIgnore
    private Long idCommunity;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_community")
    @JsonIgnore
    private Community community;

    int deletedPosts;
    int bannedUsers;
    int bannedCitizens;

    public void addDeletedPosts() {
        deletedPosts++;
    }

    public void addBannedUsers() {
        bannedUsers++;
    }

    public void addBannedCitizens() {
        bannedCitizens++;
    }

    public void clearData() {
        deletedPosts = 0;
        bannedCitizens = 0;
        bannedUsers = 0;
    }
}
