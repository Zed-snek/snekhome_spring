package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.entities.Community;
import ed.back_snekhome.entities.CommunityCitizenParameters;
import ed.back_snekhome.entities.CommunityRole;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.repositories.CitizenParametersRepository;
import ed.back_snekhome.repositories.CommunityRepository;
import ed.back_snekhome.repositories.CommunityRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityRoleRepository communityRoleRepository;
    private final CitizenParametersRepository citizenParametersRepository;
    private final UserService userService;

    @Transactional
    public void newCommunity(NewCommunityDto dto) {

        var community = Community.builder()
                .type( dto.getType() )
                .groupname( dto.getIdName() )
                .name( dto.getName() )
                .description( dto.getDescription() )
                .isClosed( dto.isClosed() )
                .isInviteUsers( dto.isInviteUsers() )
                .owner( userService.getCurrentUser() )
                .build();


        if (dto.getType() == CommunityType.NEWSPAPER) {
            community.setAnonAllowed(false);
        }
        else {
            community.setAnonAllowed(dto.isAnonAllowed());
        }

        communityRepository.save(community);

        if (dto.getType() == CommunityType.DEMOCRACY) {
            var citizenRole = CommunityRole.builder()
                    .title( dto.getTitle() )
                    .bannerColor( dto.getBannerColor() )
                    .textColor( dto.getTextColor() )
                    .makePosts( true )
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

}
