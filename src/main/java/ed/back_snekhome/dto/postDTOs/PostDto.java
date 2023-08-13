package ed.back_snekhome.dto.postDTOs;

import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.enums.RatingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;

@Getter
@Setter
@Builder
public class PostDto {

    private Post post;
    private int comments;
    private int rating;
    private RatingType ratedType;
    private ArrayList<CommentaryDto> commentaries;

    private String userName;
    private String userSurname;
    private String userNickname;
    private String userImage;
    private CommunityRole role;

    private String groupImage;
    private String groupname;
    private String groupTitle;
    private LocalDate communityDate;

}
