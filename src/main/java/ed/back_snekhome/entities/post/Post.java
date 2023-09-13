package ed.back_snekhome.entities.post;


import com.fasterxml.jackson.annotation.JsonIgnore;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPost;

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Commentary> commentaries;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PostRating> ratings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_community")
    @JsonIgnore
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account")
    @JsonIgnore
    private UserEntity user;

    private boolean isAnonymous;

    @Column(columnDefinition = "TEXT")
    private String text;

    private LocalDateTime date;

}
