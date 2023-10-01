package ed.back_snekhome.services;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.repositories.community.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembershipMethodsService {

    private final UserMethodsService userMethodsService;
    private final MembershipRepository membershipRepository;

    public Membership getMembershipOrThrowErr(UserEntity user, Community community) {
        return membershipRepository.findByCommunityAndUser(community, user)
                .orElseThrow(() -> new EntityNotFoundException("User is not a member"));
    }
    public Membership getMembershipOfCurrentUserOrThrowErr(Community community) {
        return getMembershipOrThrowErr(userMethodsService.getCurrentUser(), community);
    }

    public List<Membership> getMembershipsByUser(UserEntity user, boolean isBanned) {
        return membershipRepository.findAllByUserAndIsBanned(user, isBanned);
    }

    public List<Membership> getMembershipsByCommunity(Community community, boolean isBanned) {
        return membershipRepository.findAllByCommunityAndIsBanned(community, isBanned);
    }

    public Optional<Membership> getOptionalMembershipOfCurrentUser(Community community) {
        if (userMethodsService.isContextUser())
            return getOptionalMembershipOfUser(community, userMethodsService.getCurrentUser());
        return Optional.empty();
    }

    public Optional<Membership> getOptionalMembershipOfUser(Community community, UserEntity user) {
        return membershipRepository.findByCommunityAndUser(community, user);
    }


}
