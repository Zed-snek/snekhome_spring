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
import ed.back_snekhome.enums.PresidencyDataType;
import ed.back_snekhome.enums.RatingType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.community.CommunityRepository;
import ed.back_snekhome.repositories.community.MembershipRepository;
import ed.back_snekhome.repositories.post.CommentaryRepository;
import ed.back_snekhome.repositories.post.PostRatingRepository;
import ed.back_snekhome.repositories.post.PostRepository;
import ed.back_snekhome.helperComponents.CommunityHelper;
import ed.back_snekhome.helperComponents.MembershipHelper;
import ed.back_snekhome.helperComponents.UserHelper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final CommunityHelper communityHelper;
    private final UserHelper userHelper;
    private final FileService fileService;
    private final MembershipHelper membershipHelper;
    private final CommunityLogService communityLogService;
    private final DemocracyService democracyService;
    private final NotificationService notificationService;

    private final MembershipRepository membershipRepository;
    private final PostRepository postRepository;
    private final PostRatingRepository postRatingRepository;
    private final CommentaryRepository commentaryRepository;
    private final CommunityRepository communityRepository;

    @Transactional
    @SneakyThrows
    public Long newPost(NewPostDto dto) {
        boolean isAnon = dto.getIsAnonymous().equals("true");
        var community = communityHelper.getCommunityByNameOrThrowErr(dto.getGroupname());
        var user = userHelper.getCurrentUser();
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
                .community(community)
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
        var user = userHelper.getCurrentUser();

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
        return postRepository.getByIdPost(id).orElseThrow(() -> new EntityNotFoundException("There is no post"));
    }


    private int countUpvotes(Post post) {
        return postRatingRepository.countByPostAndType(post, RatingType.UPVOTE);
    }


    private int countRating(Post post) {
        return countUpvotes(post) - postRatingRepository.countByPostAndType(post, RatingType.DOWNVOTE);
    }


    private RatingType getRatedType(Post post) {
        if (userHelper.isContextUser()) {
            var rating = postRatingRepository.getTopByPostAndUser(post, userHelper.getCurrentUser());
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
        var membership = membershipHelper.getOptionalMembershipOfCurrentUser(post.getCommunity());

        communityHelper.throwErrIfNoAccessToCommunity(post.getCommunity(), membership);

        var postDto = setMainInfo(post)
                .groupImage(communityHelper.getTopCommunityImage(post.getCommunity()))
                .groupname(post.getCommunity().getGroupname())
                .groupTitle(post.getCommunity().getName())
                .communityDate(post.getCommunity().getCreation());
        if (!post.isAnonymous()) {
            postDto.userImage(userHelper.getTopUserImage(post.getUser()))
                    .userNickname(post.getUser().getNickname())
                    .userName(post.getUser().getName())
                    .userSurname(post.getUser().getSurname());
        }
        membership.ifPresent(value -> postDto.role(value.getRole()));

        return postDto.build();
    }


    private PostRating findPostRatingOrCreate(Post post) {
        var currentUser = userHelper.getCurrentUser();

        return postRatingRepository.getTopByPostAndUser(post, currentUser)
                .orElseGet(() -> PostRating.builder().post(post).build());
    }


    public void ratePost(Long id, RatingType newStatus) {
        var post = getPostById(id);
        var rating = findPostRatingOrCreate(post);
        rating.setType(newStatus);
        postRatingRepository.save(rating);

        notificationService.createUpvotesNotification(post, countUpvotes(post));
    }


    @Transactional
    @SneakyThrows
    public void deletePost(Long id) {
        var post = getPostById(id);
        var user = userHelper.getCurrentUser();
        var membership = membershipHelper.getOptionalMembershipOfCurrentUser(post.getCommunity());
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

                if (post.getCommunity().getType() == CommunityType.DEMOCRACY) {
                    democracyService.addStatsToPresidency(post.getCommunity(), PresidencyDataType.DELETED_POST);
                }
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
        if (!post.getImages().isEmpty() || post.getText().length() > 700)
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
        var builder = setMainInfo(post).comments(countComments(post))
                .commentaries(get2CommentsByPost(post));
        if (isCommunity) {
            builder.groupname(post.getCommunity().getGroupname())
                    .groupTitle(post.getCommunity().getName())
                    .groupImage(communityHelper.getTopCommunityImage(post.getCommunity()));
        }
        if (isUser) {
            builder.userNickname(post.getUser().getNickname())
                    .userImage(userHelper.getTopUserImage(post.getUser()));
        }
        return builder;
    }


    public List<PostDto> getPostDtoListByUser(String nickname, int pageNumber, int pageSize) {
        var user = userHelper.getUserByNicknameOrThrowErr(nickname);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        boolean isContext = userHelper.isContextUser();
        boolean isCurrentUser = isContext && userHelper.isCurrentUserEqual(user);

        List<Post> posts;
        if (isCurrentUser) {
            posts = postRepository.getPostsByUserOrderByIdPostDesc(user, pageable);
        }
        else if (isContext) {
            var currentUser = userHelper.getCurrentUser();
            List<Community> communities = communityRepository.getClosedCommunitiesByUser(currentUser);
            posts = postRepository.getPostsByNotCurrentUser(user, communities, pageable);
        }
        else {
            posts = postRepository.getPostsByUserAndIsAnonymousFalseAndCommunity_IsClosedFalseOrderByIdPostDesc(user, pageable);
        }

        return posts.stream()
                .map(post ->
                        setPostItemInfo(post, true, false)
                        .isCurrentUserAuthor(isCurrentUser)
                        .build())
                .toList();
    }


    public List<PostDto> getPostDtoListByCommunity(String groupname, int pageNumber, int pageSize, String type) { //types: hot/new
        var community = communityHelper.getCommunityByNameOrThrowErr(groupname);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Post> posts;
        if (type.equals("HOT")) {
            var startDate = LocalDateTime.now().minusDays(30);
            posts = postRepository.getPopularPostsBeforeDateByCommunity(community, startDate);
        }
        else { //if "new"
            posts = postRepository.getPostsByCommunityOrderByIdPostDesc(community, pageable);
        }

        var user = userHelper.isContextUser() ? userHelper.getCurrentUser() : null;

        return posts.stream().map(post -> {
            var dto = setPostItemInfo(post, false, !post.isAnonymous());
            dto.isCurrentUserAuthor(post.getUser().equals(user));
            if (!post.isAnonymous()) {
                var membership = membershipHelper
                        .getOptionalMembershipOfUser(post.getCommunity(), post.getUser());
                if (membership.isPresent() && membership.get().getRole() != null) {
                    var role = membership.get().getRole();
                    dto.roleBannerColor(role.getBannerColor())
                            .roleTextColor(role.getTextColor())
                            .roleTitle(role.getTitle());
                }
            }
            return dto.build();
        }).toList();
    }


    public List<PostDto> getPostDtoListHome(int pageNumber, int pageSize) {
        var user = userHelper.getCurrentUser();
        var memberships = membershipHelper.getMembershipsByUser(user, false);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        var communities = memberships
                .stream()
                .map(Membership::getCommunity)
                .toList();

        return postRepository.getPostsByCommunitiesOrderByIdPostDesc(communities, pageable)
                .stream()
                .map(post -> setPostItemInfo(post, true, !post.isAnonymous()).build())
                .toList();
    }

}


