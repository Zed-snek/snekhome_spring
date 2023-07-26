package ed.back_snekhome.services;

import ed.back_snekhome.dto.postDTOs.NewPostDto;
import ed.back_snekhome.dto.postDTOs.PostDto;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.post.PostRating;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.MembershipRepository;
import ed.back_snekhome.repositories.PostRatingRepository;
import ed.back_snekhome.repositories.PostRepository;
import ed.back_snekhome.utils.ListFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final CommunityMethodsService communityMethodsService;
    private final UserMethodsService userMethodsService;
    private final MembershipRepository membershipRepository;
    private final PostRepository postRepository;
    private final PostRatingRepository postRatingRepository;
    private final FileService fileService;


    @Transactional
    public Long newPost(NewPostDto dto) throws IOException {
        boolean isAnon = dto.getIsAnonymous().equals("true");
        var community = communityMethodsService.getCommunityByName(dto.getGroupname());
        var user = userMethodsService.getCurrentUser();
        var membership = membershipRepository.findByCommunityAndUser(community, user);
        if (membership.isEmpty() && community.isClosed()
                || membership.isPresent() && (membership.get().isBanned()
                || community.getType() == CommunityType.NEWSPAPER && membership.get().getRole() == null)
        ) {
            throw new UnauthorizedException("Unauthorized to make posts in this community");
        }
        if (isAnon && !community.isAnonAllowed())
            throw new BadRequestException("Action is not possible due to community rules");

        var post = Post.builder()
                .date(LocalDateTime.now())
                .community(community)
                .user(user)
                .text(dto.getText())
                .isAnonymous(isAnon)
                .build();
        postRepository.save(post);
        fileService.uploadPostImages(dto.getImages(), post);
        return post.getIdPost();
    }

    public Post getPostById(Long id) {
        return postRepository.getByIdPost(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no post"));
    }

    private int countRating(Post post) {
        return postRatingRepository.countByPostAndType(post, RatingType.UPVOTE)
                - postRatingRepository.countByPostAndType(post, RatingType.DOWNVOTE);
    }
    private RatingType getRatedType(Post post) {
        if (userMethodsService.isContextUser()) {
            var rating =
                    postRatingRepository.getTopByPostAndUser(post, userMethodsService.getCurrentUser());
            if (rating.isPresent())
                return rating.get().getType();
        }
        return RatingType.NONE;
    }

    public PostDto getPostPage(Long id) {
        var post = getPostById(id);
        var membership =
                communityMethodsService.getOptionalMembershipOfCurrentUser(post.getCommunity());

        if (communityMethodsService.isAccessToCommunity(post.getCommunity(), membership)) {
            var postDto = PostDto.builder()
                    .post(post)
                    .ratedType(getRatedType(post))
                    .rating(countRating(post))
                    .groupImage(ListFunctions.getTopImageOfList(post.getCommunity().getImages()))
                    .groupname(post.getCommunity().getGroupname())
                    .groupTitle(post.getCommunity().getName())
                    .communityDate(post.getCommunity().getCreation())
                    .build();
            if (!post.isAnonymous()) {
                postDto.setUserImage(ListFunctions.getTopImageOfList(post.getUser().getImages()));
                postDto.setUserNickname(post.getUser().getNickname());
                postDto.setUserName(post.getUser().getName());
                postDto.setUserSurname(post.getUser().getSurname());
                membership.ifPresent(value -> postDto.setRole(value.getRole()));
            }
            return postDto;
        }
        else {
            throw new UnauthorizedException("No access to post");
        }
    }

    private PostRating findPostRatingOrCreate(Post post) {
        var currentUser = userMethodsService.getCurrentUser();
        var rating = postRatingRepository.getTopByPostAndUser(post, currentUser);
        if (rating.isEmpty())
            return PostRating.builder()
                    .post(post)
                    .user(currentUser)
                    .build();
        else
            return rating.get();
    }

    public void ratePost(Long id, RatingType newStatus) {
        var post = getPostById(id);
        var rating = findPostRatingOrCreate(post);
        rating.setType(newStatus);
        postRatingRepository.save(rating);
    }




}


