package ed.back_snekhome.controllers;

import ed.back_snekhome.dto.communityDTOs.GeneralDemocracyDto;
import ed.back_snekhome.services.DemocracyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DemocracyController {

    private final DemocracyService democracyService;

    @GetMapping("/democracy/{name}")
    public GeneralDemocracyDto getCommunity(@PathVariable String name) {
        return democracyService.getGeneralDemocracyData(name);
    }

}
