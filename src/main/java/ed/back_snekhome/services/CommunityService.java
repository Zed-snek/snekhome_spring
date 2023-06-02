package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityDto;
import ed.back_snekhome.entities.*;
import ed.back_snekhome.entities.relations.Membership;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.repositories.CitizenParametersRepository;
import ed.back_snekhome.repositories.CommunityRepository;
import ed.back_snekhome.repositories.CommunityRoleRepository;
import ed.back_snekhome.repositories.MembershipRepository;
import ed.back_snekhome.utils.ListFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
            if (dto.getType() == CommunityType.DEMOCRACY)
                title = "president";
            else
                title = "owner";

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
                    .isCitizen(false)
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
            if (membership.isPresent())
                dto.setMember(true);
        }
        return dto;
    }

    public Community getCommunityByName(String name) {
        return communityRepository.findByGroupname(name).orElseThrow(() -> new EntityNotFoundException("Community is not found"));
    }

}
