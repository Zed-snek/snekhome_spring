package ed.back_snekhome.dto.postDTOs;

import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.entities.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PostDto {

    private Post post;
    private int comments;
    private int rating;
    private boolean isRated;
    private boolean isAnonymous;
    private List<Commentary> commentaries;

    private String username;
    private String userImage;
    private CommunityRole role;

    private String groupImage;
    private String groupname;
    private String groupTitle;

}
