package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.NewCommunityDto;
import ed.back_snekhome.dto.communityDTOs.PublicCommunityDto;
import ed.back_snekhome.entities.*;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.repositories.CitizenParametersRepository;
import ed.back_snekhome.repositories.CommunityRepository;
import ed.back_snekhome.repositories.CommunityRoleRepository;
import ed.back_snekhome.utils.ListFunctions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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
                .creation( LocalDate.now() )
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

    public PublicCommunityDto getPublicCommunityDto(String name) {
        var community = communityRepository.findByGroupname(name);
        var dto = PublicCommunityDto.builder()
                .community( community )
                .members( 66 )
                .ownerNickname( community.getOwner().getNickname() )
                .ownerImage( ListFunctions.getTopImageOfList(community.getOwner().getImages()) )
                .build();
        return dto;
    }

    private String getTopImageOfCommunity(List<CommunityImage> images) {
        if (images.size() == 0) {
            return "";
        }
        else {
            return  images.get( images.size() - 1 ).getName();
        }
    }

}
