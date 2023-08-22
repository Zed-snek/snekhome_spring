package ed.back_snekhome.services;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityMethodsService {

    private final CommunityRepository communityRepository;
    private final CommunityImageRepository communityImageRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final MembershipRepository membershipRepository;
    private final UserMethodsService userMethodsService;

    public int countMembers(Community community) {
        return membershipRepository.countAllByCommunity(community);
    }

    public boolean isContextUserMember(Community community) {
        if (userMethodsService.isContextUser()) {
            var membership
                    = membershipRepository.findByCommunityAndUser(community, userMethodsService.getCurrentUser());
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
        return userMethodsService.isCurrentUserEqual(community.getOwner());
    }

    public boolean isAccessToCommunity(Community community, Optional<Membership> membership) {
        return (membership.isPresent() && !membership.get().isBanned())
                || !community.isClosed()
                || isCurrentUserOwner(community);
    }

    public String getTopCommunityImage(Community community) {
        var img = communityImageRepository.findTopByCommunityOrderByIdImageDesc(community);
        if (img.isPresent())
            return img.get().getName();
        return "";
    }

}
