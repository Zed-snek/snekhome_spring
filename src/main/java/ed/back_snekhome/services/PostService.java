package ed.back_snekhome.services;

import ed.back_snekhome.dto.postDTOs.NewPostDto;
import ed.back_snekhome.entities.post.Post;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.MembershipRepository;
import ed.back_snekhome.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final CommunityMethodsService communityMethodsService;
    private final UserMethodsService userMethodsService;
    private final MembershipRepository membershipRepository;
    private final PostRepository postRepository;
    private final FileService fileService;

    @Transactional
    public void newPost(NewPostDto dto) throws IOException {
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
    }

}


