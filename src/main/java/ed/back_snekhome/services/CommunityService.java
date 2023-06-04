package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityCardDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityDto;
import ed.back_snekhome.dto.communityDTOs.UpdateCommunityDto;
import ed.back_snekhome.entities.*;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.CitizenParametersRepository;
import ed.back_snekhome.repositories.CommunityRepository;
import ed.back_snekhome.repositories.CommunityRoleRepository;
import ed.back_snekhome.repositories.MembershipRepository;
import ed.back_snekhome.utils.ListFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final CitizenParametersRepository citizenParametersRepository;
    private final UserService userService;
    private final MembershipRepository membershipRepository;

    @Transactional
    public void newCommunity(NewCommunityDto dto) {
        var owner = userService.getCurrentUser();

        var community = Community.builder()
                .type( dto.getType() )
                .groupname( dto.getIdName() )
                .name( dto.getName() )
                .description( dto.getDescription() )
                .isClosed( dto.isClosed() )
                .isInviteUsers( dto.isInviteUsers() )
                .owner( owner )
                .creation( LocalDate.now() )
                .build();

        if (dto.getType() == CommunityType.NEWSPAPER) {
            community.setAnonAllowed(false);
        }
        else {
            community.setAnonAllowed(dto.isAnonAllowed());
        }
        communityRepository.save(community);

        var membership = Membership.builder()
                .user(owner)
                .community(community)
                .build();

        CommunityRole ownerRole;
        if (!(dto.getType() == CommunityType.ANARCHY)) {
            String title;
            boolean isCitizen;
            if (dto.getType() == CommunityType.DEMOCRACY) {
                title = "president";
                isCitizen = true;
            }
            else {
                title = "owner";
                isCitizen = false;
            }

            ownerRole = CommunityRole.builder()
                    .title(title)
                    .community(community)
                    .bannerColor("#15151D")
                    .textColor("#E3E3E3")
                    .banCitizen(true)
                    .deletePosts(true)
                    .editDescription(true)
                    .banUser(true)
                    .editId(true)
                    .isCitizen(isCitizen)
                    .isCreator(true)
                    .build();
        }
        else {
            ownerRole = CommunityRole.builder()
                    .title("creator")
                    .community(community)
                    .bannerColor("#15151D")
                    .textColor("#E3E3E3")
                    .banCitizen(false)
                    .deletePosts(false)
                    .editDescription(false)
                    .banUser(false)
                    .editId(false)
                    .isCitizen(false)
                    .isCreator(true)
                    .build();
        }
        communityRoleRepository.save(ownerRole);
        membership.setRole(ownerRole);
        membershipRepository.save(membership);

        if (dto.getType() == CommunityType.DEMOCRACY) {
            var citizenRole = CommunityRole.builder()
                    .title( dto.getTitle() )
                    .bannerColor( dto.getBannerColor() )
                    .textColor( dto.getTextColor() )
                    .isCitizen( true )
                    .community( community )
                    .build();
            communityRoleRepository.save(citizenRole);

            var citizenParameters = CommunityCitizenParameters.builder()
                    .days( dto.getCitizenDays() )
                    .rating( dto.getCitizenRating() )
                    .electionDays( dto.getElectionDays() )
                    .community( community )
                    .build();
            citizenParametersRepository.save(citizenParameters);
        }

    }

    public void deleteCommunity(String name) {
        var community = getCommunityByName(name);
        if (community.getOwner().equals(userService.getCurrentUser()) && community.getType() != CommunityType.DEMOCRACY)
            communityRepository.delete(community);
        else
            throw new UnauthorizedException("User doesn't have permissions to delete the community");
    }

    public boolean isNameTaken(String name) {
        userService.throwErrIfExistsByNickname(name);
        return true;
    }

    private int countMembers(String name) {
        return membershipRepository.countAllByCommunity(getCommunityByName(name));
    }


    public PublicCommunityDto getPublicCommunityDto(String name) {
        var community = getCommunityByName(name);
        var dto = PublicCommunityDto.builder()
                .community( community )
                .members( countMembers(name) )
                .ownerNickname( community.getOwner().getNickname() )
                .ownerImage( ListFunctions.getTopImageOfList(community.getOwner().getImages()) )
                .build();
        dto.setMember(false);
        if (userService.isContextUser()) {
            var membership = membershipRepository.findByCommunityAndUser(community, userService.getCurrentUser());
            if (membership.isPresent()) {
                dto.setMember(true);
                dto.setCurrentUserRole(membership.get().getRole());
            }
        }
        return dto;
    }

    public Community getCommunityByName(String name) {
        return communityRepository.findByGroupname(name).orElseThrow(() -> new EntityNotFoundException("Community is not found"));
    }

    public ArrayList<PublicCommunityCardDto> getHomeCards() {
        var list = membershipRepository.findTop4ByUser(userService.getCurrentUser());
        var array = new ArrayList<PublicCommunityCardDto>();
        list.forEach(o ->
                array.add(PublicCommunityCardDto.builder()
                        .image( ListFunctions.getTopImageOfList(o.getCommunity().getImages()) )
                        .groupname( o.getCommunity().getGroupname() )
                        .build()));
        return array;
    }

    public void updateCommunity(UpdateCommunityDto dto) {
        var user = userService.getCurrentUser();
        var community = getCommunityByName(dto.getOldGroupname());
        var membership = membershipRepository.findByCommunityAndUser(community, user);
        if (membership.isPresent() && membership.get().getRole() != null) {
            var role = membership.get().getRole();

            if (dto.getGroupname() != null) {
                if (role.isEditId()) {
                    isNameTaken(dto.getGroupname());
                    community.setGroupname(dto.getGroupname());
                }
                else
                    throw new UnauthorizedException("User doesn't have permissions");
            }
            else if (role.isEditDescription()) {
                if (dto.getDescription() != null)
                    community.setDescription(dto.getDescription());
                else if (dto.getName() != null)
                    community.setName(dto.getName());
            }
            else
                throw new UnauthorizedException("User doesn't have permissions");
            communityRepository.save(community);
        }

    }

}
