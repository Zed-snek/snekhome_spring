package ed.back_snekhome.entities.post;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.community.Community;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long idPost;

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PostImage> images;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Commentary> commentaries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_community")
    @JsonIgnore
    private Community community;

    private boolean isAnonymous;

    @Column(columnDefinition = "TEXT")
    private String text;
}
