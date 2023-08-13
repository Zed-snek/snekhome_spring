package ed.back_snekhome.services;

import ed.back_snekhome.dto.postDTOs.CommentaryDto;
import ed.back_snekhome.dto.postDTOs.EditPostDto;
import ed.back_snekhome.dto.postDTOs.NewPostDto;
import ed.back_snekhome.dto.postDTOs.PostDto;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.post.PostImage;
import ed.back_snekhome.entities.post.PostRating;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.CommentaryRepository;
import ed.back_snekhome.repositories.MembershipRepository;
import ed.back_snekhome.repositories.PostRatingRepository;
import ed.back_snekhome.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PostService {

    private final CommunityMethodsService communityMethodsService;
    private final UserMethodsService userMethodsService;
    private final FileService fileService;

    private final MembershipRepository membershipRepository;
    private final PostRepository postRepository;
    private final PostRatingRepository postRatingRepository;
    private final CommentaryRepository commentaryRepository;

    @Transactional
    public Long newPost(NewPostDto dto) throws IOException {
        boolean isAnon = dto.getIsAnonymous().equals("true");
        var community = communityMethodsService.getCommunityByNameOrThrowErr(dto.getGroupname());
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

        if (dto.getImages().size() > 10)
            throw new BadRequestException("Images limit");

        var post = Post.builder()
                .date(LocalDateTime.now())
                .community(community)
                .user(user)
                .text(dto.getText())
                .isAnonymous(isAnon)
                .build();
        postRepository.save(post);
        fileService.uploadPostImages(dto.getImages(), post);

        var rating = PostRating.builder()
                .post(post)
                .user(user)
                .type(RatingType.UPVOTE)
                .build();
        postRatingRepository.save(rating);
        return post.getIdPost();
    }

    @Transactional
    public void updatePost(EditPostDto dto, Long id) throws IOException {
        var post = getPostById(id);
        var user = userMethodsService.getCurrentUser();

        if (dto.getOldImages().size() + dto.getNewImages().size() > 10)
            throw new BadRequestException("Images limit");

        if (post.getUser().equals(user)) {
            post.setText(dto.getText());
            postRepository.save(post);

            var postImages = post.getImages();
            String currentImg;
            for (PostImage postImage : postImages) {
                currentImg = postImage.getName();
                if (!dto.getOldImages().contains(currentImg)) {
                    fileService.deletePostImageByName(currentImg);
                }
            }
            fileService.uploadPostImages(dto.getNewImages(), post);
        }
        else
            throw new UnauthorizedException("No access to edit the post");
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

    private PostDto.PostDtoBuilder setMainInfo(Post post) {
        return PostDto.builder()
                .post(post)
                .rating(countRating(post))
                .ratedType(getRatedType(post));
    }

    public PostDto getPostPage(Long id) {
        var post = getPostById(id);
        var membership =
                communityMethodsService.getOptionalMembershipOfCurrentUser(post.getCommunity());

        if (communityMethodsService.isAccessToCommunity(post.getCommunity(), membership)) {
            var postDto = setMainInfo(post)
                    .groupImage(communityMethodsService.getTopCommunityImage(post.getCommunity()))
                    .groupname(post.getCommunity().getGroupname())
                    .groupTitle(post.getCommunity().getName())
                    .communityDate(post.getCommunity().getCreation());
            if (!post.isAnonymous()) {
                postDto
                        .userImage(userMethodsService.getTopUserImage(post.getUser()))
                        .userNickname(post.getUser().getNickname())
                        .userName(post.getUser().getName())
                        .userSurname(post.getUser().getSurname());
            }
            membership.ifPresent(value -> postDto.role(value.getRole()));
            return postDto.build();
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

    @Transactional
    public void deletePost(Long id) throws FileNotFoundException {
        var post = getPostById(id);
        var user = userMethodsService.getCurrentUser();
        var membership =
                communityMethodsService.getOptionalMembershipOfCurrentUser(post.getCommunity());
        if (post.getUser().equals(user) || (membership.isPresent() && membership.get().getRole().isDeletePosts())) {

            for (PostImage img : post.getImages())
                fileService.deletePostImageByName(img.getName());

            postRepository.delete(post);
        }
        else
            throw new UnauthorizedException("No access to delete post");
    }

    private int countComments(Post post) {
        return commentaryRepository.countAllByPost(post);
    }
    private ArrayList<CommentaryDto> get2CommentsByPost(Post post) {
        var list = commentaryRepository.findTop2ByPostOrderByIdCommentaryAsc(post);
        var array = new ArrayList<CommentaryDto>();
        list.forEach(c -> array.add(
                CommentaryDto.builder()
                        .text(c.getText())
                        .nickname(c.getUser().getNickname())
                        .build())
        );
        return array;
    }

    public ArrayList<PostDto> getPostDtoListByUser(String nickname) {
        var array = new ArrayList<PostDto>();
        var user = userMethodsService.getUserByNickname(nickname);
        var posts = postRepository.getPostsByUser(user);
        boolean isCurrentUser;
        if (userMethodsService.isContextUser())
             isCurrentUser = userMethodsService.isCurrentUserEqual(user);
        else
            isCurrentUser = false;

        for (Post post : posts) {
            if (!(post.isAnonymous() && !isCurrentUser)) {
                array.add(setMainInfo(post)
                        .groupname(post.getCommunity().getGroupname())
                        .groupTitle(post.getCommunity().getName())
                        .groupImage(communityMethodsService.getTopCommunityImage(post.getCommunity()))
                        .comments(countComments(post))
                        .commentaries(get2CommentsByPost(post))
                        .build());
            }
        }
        return array;
    }
    public ArrayList<PostDto> getPostDtoListByCommunity(String groupname) {
        return null;
    }
    public ArrayList<PostDto> getPostDtoListHome() {
        return null;
    }


}


