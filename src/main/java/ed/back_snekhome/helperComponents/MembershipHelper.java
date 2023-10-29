package ed.back_snekhome.helperComponents;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.repositories.community.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MembershipHelper {

    private final UserHelper userHelper;
    private final MembershipRepository membershipRepository;

    public Membership getMembershipOrThrowErr(UserEntity user, Community community) {
        return membershipRepository.findByCommunityAndUser(community, user)
                .orElseThrow(() -> new EntityNotFoundException("User is not a member"));
    }
    public Membership getMembershipOfCurrentUserOrThrowErr(Community community) {
        return getMembershipOrThrowErr(userHelper.getCurrentUser(), community);
    }

    public List<Membership> getMembershipsByUser(UserEntity user, boolean isBanned) {
        return membershipRepository.findAllByUserAndIsBanned(user, isBanned);
    }

    public List<Membership> getMembershipsByCommunity(Community community, boolean isBanned) {
        return membershipRepository.findAllByCommunityAndIsBanned(community, isBanned);
    }

    public Optional<Membership> getOptionalMembershipOfCurrentUser(Community community) {
        if (userHelper.isContextUser())
            return getOptionalMembershipOfUser(community, userHelper.getCurrentUser());
        return Optional.empty();
    }

    public Optional<Membership> getOptionalMembershipOfUser(Community community, UserEntity user) {
        return membershipRepository.findByCommunityAndUser(community, user);
    }


}
