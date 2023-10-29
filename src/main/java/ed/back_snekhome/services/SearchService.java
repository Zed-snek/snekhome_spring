package ed.back_snekhome.services;

import ed.back_snekhome.dto.searchDTOs.SearchItemDto;
import ed.back_snekhome.dto.searchDTOs.SearchUserCommunityDto;
import ed.back_snekhome.repositories.community.CommunityRepository;
import ed.back_snekhome.repositories.user.UserRepository;
import ed.back_snekhome.helperComponents.CommunityHelper;
import ed.back_snekhome.helperComponents.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final UserHelper userHelper;
    private final CommunityHelper communityHelper;

    public List<SearchItemDto> findCommunitiesByRequest(String request, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        var list =
                communityRepository.searchCommunitiesByRequest(request, pageable);
        return list.stream().map(community -> SearchItemDto
                        .builder()
                        .idName(community.getGroupname())
                        .image(communityHelper.getTopCommunityImage(community))
                        .title(community.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SearchItemDto> findUsersByRequest(String request, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        var list = userRepository.searchUsersByRequest(request, pageable);
        return list.stream().map(user -> SearchItemDto
                        .builder()
                        .idName(user.getNickname())
                        .image(userHelper.getTopUserImage(user))
                        .title(user.getName() + " " + user.getSurname())
                        .build())
                .collect(Collectors.toList());
    }

    public SearchUserCommunityDto findByRequest(String request) {
        return SearchUserCommunityDto.builder()
                .communities(findCommunitiesByRequest(request, 0, 4))
                .users(findUsersByRequest(request, 0, 4))
                .build();
    }

}
