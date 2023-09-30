package ed.back_snekhome.services;

import ed.back_snekhome.dto.postDTOs.CommentaryDto;
import ed.back_snekhome.dto.postDTOs.EditPostDto;
import ed.back_snekhome.dto.postDTOs.NewPostDto;
import ed.back_snekhome.dto.postDTOs.PostDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.post.Commentary;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.post.PostImage;
import ed.back_snekhome.entities.post.PostRating;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.community.CommunityRepository;
import ed.back_snekhome.repositories.community.MembershipRepository;
import ed.back_snekhome.repositories.post.CommentaryRepository;
import ed.back_snekhome.repositories.post.PostRatingRepository;
import ed.back_snekhome.repositories.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final CommunityMethodsService communityMethodsService;
    private final UserMethodsService userMethodsService;
    private final FileService fileService;
    private final MembershipService membershipService;
    private final CommunityLogService communityLogService;

    private final MembershipRepository membershipRepository;
    private final PostRepository postRepository;
    private final PostRatingRepository postRatingRepository;
    private final CommentaryRepository commentaryRepository;
    private final CommunityRepository communityRepository;

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

            var postImages = post.getImages();
            List<PostImage> toDelete = new ArrayList<>();

            for (PostImage postImage : postImages) {
                if (!dto.getOldImages().contains(postImage.getName())) {
                    toDelete.add(postImage);
                    fileService.deleteImageFromStorage(postImage.getName());
                }
            }
            postImages.removeAll(toDelete);

            postRepository.save(post);
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
                membershipService.getOptionalMembershipOfCurrentUser(post.getCommunity());

        communityMethodsService.throwErrIfNoAccessToCommunity(post.getCommunity(), membership);

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
                membershipService.getOptionalMembershipOfCurrentUser(post.getCommunity());
        boolean isCurrentUserAuthor = post.getUser().equals(user);
        boolean isDeletePermit = membership.isPresent() && membership.get().getRole().isDeletePosts();

        if (isCurrentUserAuthor || isDeletePermit) {
            for (PostImage img : post.getImages()) {
                fileService.deleteImageFromStorage(img.getName());
            }
            if (isDeletePermit && !isCurrentUserAuthor) {
                String text = post.getText();
                communityLogService.createLogDeletePost(
                        post.getCommunity(),
                        post.getUser(),
                        text.length() > 100 ? text.substring(0, 100) : text
                );
            }
            postRepository.delete(post);
        }
        else
            throw new UnauthorizedException("No access to delete post");
    }

    private int countComments(Post post) {
        return commentaryRepository.countAllByPost(post);
    }
    private List<CommentaryDto> get2CommentsByPost(Post post) {
        Long ref = (long) -1;
        List<Commentary> list;
        if (post.getImages().size() > 0 || post.getText().length() > 700)
            list = commentaryRepository.findTop2ByPostAndReferenceIdOrderByIdCommentaryAsc(post, ref);
        else if (post.getText().length() > 350)
            list = commentaryRepository.findTopByPostAndReferenceIdOrderByIdCommentaryAsc(post, ref);
        else
            return null;

        var array = new ArrayList<CommentaryDto>();
        list.forEach(c -> array.add(
                CommentaryDto.builder()
                        .text(c.getText())
                        .nickname(c.getUser().getNickname())
                        .build())
        );
        return array;
    }
    private PostDto.PostDtoBuilder setPostItemInfo(Post post, boolean isCommunity, boolean isUser) {
        var builder = setMainInfo(post)
                    .comments(countComments(post))
                    .commentaries(get2CommentsByPost(post));
        if (isCommunity) {
            builder
                    .groupname(post.getCommunity().getGroupname())
                    .groupTitle(post.getCommunity().getName())
                    .groupImage(communityMethodsService.getTopCommunityImage(post.getCommunity()));
        }
        if (isUser) {
            builder
                    .userNickname(post.getUser().getNickname())
                    .userImage(userMethodsService.getTopUserImage(post.getUser()));
        }
        return builder;
    }

    public List<PostDto> getPostDtoListByUser(String nickname, int pageNumber, int pageSize) {
        var user = userMethodsService.getUserByNicknameOrThrowErr(nickname);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        boolean isContext = userMethodsService.isContextUser();
        boolean isCurrentUser = isContext && userMethodsService.isCurrentUserEqual(user);

        List<Post> posts;
        if (isCurrentUser)
            posts = postRepository.getPostsByUserOrderByIdPostDesc(user, pageable);
        else if (isContext) {
            var currentUser = userMethodsService.getCurrentUser();
            List<Community> communities = communityRepository.getCommunitiesByUser(currentUser);
            posts = postRepository.getPostsByNotCurrentUserOrderByIdPostDesc(user, communities, pageable);
        }
        else {
            posts = postRepository.getPostsByUserAndIsAnonymousOrderByIdPostDesc(user, false, pageable);
        }

        var array = new ArrayList<PostDto>();
        for (Post post : posts) {
            if (!(post.isAnonymous() && !isCurrentUser)) {
                array.add(setPostItemInfo(post, true, false)
                            .isCurrentUserAuthor(isCurrentUser)
                            .build()
                );
            }
        }
        return array;
    }

    public List<PostDto> getPostDtoListByCommunity(String groupname, int pageNumber, int pageSize) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        var posts = postRepository.getPostsByCommunityOrderByIdPostDesc(community, pageable);

        var user = userMethodsService.isContextUser() ? userMethodsService.getCurrentUser() : null;

        var array = new ArrayList<PostDto>();
        for (Post post : posts) {
            var dto = setPostItemInfo(post, false, !post.isAnonymous());
            dto.isCurrentUserAuthor(post.getUser().equals(user));
            if (!post.isAnonymous()) {
                var membership
                        = membershipService.getOptionalMembershipOfUser(post.getCommunity(), post.getUser());
                if (membership.isPresent() && membership.get().getRole() != null) {
                    var role = membership.get().getRole();
                    dto
                            .roleBannerColor(role.getBannerColor())
                            .roleTextColor(role.getTextColor())
                            .roleTitle(role.getTitle());
                }
            }
            array.add(dto.build());
        }
        return array;
    }

    public List<PostDto> getPostDtoListHome(int pageNumber, int pageSize) {
        var user = userMethodsService.getCurrentUser();
        var memberships = membershipService.getMembershipsByUser(user, false);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        var communities = memberships
                .stream()
                .map(Membership::getCommunity)
                .toList();

        return postRepository.getPostsByCommunitiesOrderByIdPostDesc(communities, pageable)
                .stream()
                .map(post -> setPostItemInfo(post, true, !post.isAnonymous())
                        .build())
                .toList();
    }

}


