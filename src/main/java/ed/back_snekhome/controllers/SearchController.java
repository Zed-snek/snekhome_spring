package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.searchDTOs.SearchItemDto;
import ed.back_snekhome.dto.searchDTOs.SearchUserCommunityDto;
import ed.back_snekhome.exceptionHandler.exceptions.BadRequestException;
import ed.back_snekhome.services.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private static final String DEFAULT_PAGE_SIZE = "20";

    private final SearchService searchService;

    @GetMapping("/{request}")
    public SearchUserCommunityDto find(@PathVariable String request) {

        log.info("Searching users and communities with request: {}", request);

        return searchService.findByRequest(request);
    }

    @GetMapping("/by_type/{type}/{request}")
    public List<SearchItemDto> findByType(
            @PathVariable String request,
            @PathVariable String type,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize
    ) {

        if (type.equals("community")) {
            log.info("Searching communities with request: {}", request);
            return searchService.findCommunitiesByRequest(request, page, pageSize);
        }
        if (type.equals("user")) {
            log.info("Searching users with request: {}", request);
            return searchService.findUsersByRequest(request, page, pageSize);
        }

        throw new BadRequestException("There is not such search type as \"" + type + "\" ");
    }

}
