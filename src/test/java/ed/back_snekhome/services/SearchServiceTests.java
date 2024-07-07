package ed.back_snekhome.services;

import ed.back_snekhome.repositories.community.CommunityRepository;
import ed.back_snekhome.repositories.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommunityRepository communityRepository;

    @InjectMocks
    private SearchService searchService;


    @Test
    public void searchService_findCommunitiesByRequest_ReturnExactCommunities() {

    }

}
