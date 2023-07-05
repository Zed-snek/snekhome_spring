package ed.back_snekhome.services;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityRole;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityMethodsService {

    private final CommunityRepository communityRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final MembershipRepository membershipRepository;
    private final UserMethodsService userMethodsService;

    public int countMembers(Community community) {
        return membershipRepository.countAllByCommunity(community);
    }

    public boolean isContextUserMember(Community community) {
        if (userMethodsService.isContextUser()) {
            var membership = membershipRepository.findByCommunityAndUser(community, userMethodsService.getCurrentUser());
            return membership.isPresent();
        }
        return false;
    }

    public Community getCommunityByName(String name) {
        return communityRepository.findByGroupname(name).orElseThrow(() -> new EntityNotFoundException("Community is not found"));
    }

    public CommunityRole findRoleOrThrowErr(Community community, String roleName) {
        return communityRoleRepository
                .findByCommunityAndTitle(community, roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role is not found"));
    }

    public boolean isCurrentUserOwner(Community community) {
        return userMethodsService.isCurrentUserEqual(community.getOwner());
    }

}
