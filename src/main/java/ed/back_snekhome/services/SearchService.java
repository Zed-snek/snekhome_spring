package ed.back_snekhome.services;

import ed.back_snekhome.dto.searchDTOs.SearchItemDto;
import ed.back_snekhome.dto.searchDTOs.SearchUserCommunityDto;
import ed.back_snekhome.repositories.community.CommunityRepository;
import ed.back_snekhome.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    public List<SearchItemDto> findCommunitiesByRequest(String request, int pageNumber, int pageSize) {
        return null;
    }

    public List<SearchItemDto> findUsersByRequest(String request, int pageNumber, int pageSize) {
        return null;
    }

    public SearchUserCommunityDto findByRequest(String request) {
        return SearchUserCommunityDto.builder()
                .communities(findCommunitiesByRequest(request, 0, 6))
                .users(findUsersByRequest(request, 0, 4))
                .build();
    }

}
