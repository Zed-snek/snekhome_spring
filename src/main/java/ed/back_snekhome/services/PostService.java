package ed.back_snekhome.services;

import ed.back_snekhome.dto.postDTOs.NewPostDto;
import ed.back_snekhome.dto.postDTOs.PostDto;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.MembershipRepository;
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
    private final FileService fileService;


    @Transactional
    public Long newPost(NewPostDto dto) throws IOException {
        var community = communityMethodsService.getCommunityByName(dto.getGroupname());
        var user = userMethodsService.getCurrentUser();
        var membership = membershipRepository.findByCommunityAndUser(community, user);
        if (membership.isEmpty() && community.isClosed()
                || membership.get().isBanned()
                || community.getType() == CommunityType.NEWSPAPER && membership.get().getRole() == null
        ) {
            throw new UnauthorizedException("Unauthorized to make posts in this community");
        }
        if (dto.isAnonymous() && !community.isAnonAllowed())
            throw new BadRequestException("Action is not possible due to community rules");

        var post = Post.builder()
                .date(LocalDateTime.now())
                .community(community)
                .user(user)
                .text(dto.getText())
                .isAnonymous(dto.isAnonymous())
                .build();
        postRepository.save(post);
        fileService.uploadPostImages(dto.getImages(), post);
        return post.getIdPost();
    }

    private Post getPostById(Long id) {
        return postRepository.getByIdPost(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no post"));
    }

    public PostDto getPostPage(Long id) {
        var post = getPostById(id);
        Optional<Membership> membership = Optional.empty();
        if (userMethodsService.isContextUser()) {
            membership = membershipRepository.findByCommunityAndUser(post.getCommunity(), userMethodsService.getCurrentUser());
        }
        if (communityMethodsService.isAccessToCommunity(post.getCommunity(), membership)) {
            var postDto = PostDto.builder()
                    .post(post)
                    .isRated(false) //todo check isRated
                    .rating(231) //todo count rating
                    .groupImage(ListFunctions.getTopImageOfList(post.getCommunity().getImages()))
                    .groupname(post.getCommunity().getGroupname())
                    .groupTitle(post.getCommunity().getName())
                    .isAnonymous(post.isAnonymous())
                    .build();
            if (!post.isAnonymous()) {
                postDto.setUserImage(ListFunctions.getTopImageOfList(post.getUser().getImages()));
                postDto.setUsername(post.getUser().getUsername());
                membership.ifPresent(value -> postDto.setRole(value.getRole()));
            }
            return postDto;
        }
        else {
            throw new UnauthorizedException("No access to post");
        }
    }

}


