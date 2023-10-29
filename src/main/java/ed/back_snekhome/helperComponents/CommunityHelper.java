package ed.back_snekhome.helperComponents;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.community.Membership;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.community.CommunityImageRepository;
import ed.back_snekhome.repositories.community.CommunityRepository;
import ed.back_snekhome.repositories.community.CommunityRoleRepository;
import ed.back_snekhome.repositories.community.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommunityHelper {

    private final CommunityRepository communityRepository;
    private final CommunityImageRepository communityImageRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final MembershipRepository membershipRepository;
    private final UserHelper userHelper;

    public int countMembers(Community community) {
        return membershipRepository.countAllByCommunityAndIsBanned(community, false);
    }

    public boolean isContextUserMember(Community community) {
        if (userHelper.isContextUser()) {
            var membership
                    = membershipRepository.findByCommunityAndUser(community, userHelper.getCurrentUser());
            return membership.isPresent();
        }
        return false;
    }

    public Community getCommunityByNameOrThrowErr(String name) {
        return communityRepository.findByGroupnameIgnoreCase(name)
                .orElseThrow(() -> new EntityNotFoundException("Community is not found"));
    }

    public CommunityRole findRoleOrThrowErr(Community community, String roleName) {
        return communityRoleRepository
                .findByCommunityAndTitle(community, roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role is not found"));
    }

    public boolean isCurrentUserOwner(Community community) {
        return userHelper.isCurrentUserEqual(community.getOwner());
    }

    public void throwErrIfNoAccessToCommunity(Community community, Optional<Membership> membership) {
        if (!isAccessToCommunity(community, membership))
            throw new UnauthorizedException("No access to community");
    }

    public boolean isAccessToCommunity(Community community, Optional<Membership> membership) {
        boolean isMember = membership.isPresent();
        boolean isBanned = isMember && membership.get().isBanned();
        return (!community.isClosed() || community.isClosed() && isMember) && !isBanned
                || isCurrentUserOwner(community);
    }

    public String getTopCommunityImage(Community community) {
        var img = communityImageRepository.findTopByCommunityOrderByIdImageDesc(community);
        if (img.isPresent())
            return img.get().getName();
        return "";
    }

}
