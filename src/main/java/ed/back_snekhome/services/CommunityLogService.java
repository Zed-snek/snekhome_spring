package ed.back_snekhome.services;

import ed.back_snekhome.dto.communityDTOs.CommunityLogDto;
import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityLog;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.CommunityType;
import ed.back_snekhome.enums.LogType;
import ed.back_snekhome.exceptionHandler.exceptions.UnauthorizedException;
import ed.back_snekhome.repositories.CommunityLogRepository;
import ed.back_snekhome.repositories.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityLogService {

    private final UserMethodsService userMethodsService;
    private final CommunityMethodsService communityMethodsService;
    private final MembershipRepository membershipRepository;
    private final CommunityLogRepository communityLogRepository;


    private void saveLog(CommunityLog.CommunityLogBuilder log) {
        communityLogRepository.save(log.build());
    }

    private CommunityLog.CommunityLogBuilder builder(UserEntity actor, Community community) {
        return CommunityLog.builder()
                .date(LocalDateTime.now())
                .actionUser(actor)
                .community(community);
    }

    private CommunityLog.CommunityLogBuilder builderWithCurrentUser(Community community) {
        return builder(userMethodsService.getCurrentUser(), community);
    }

    private <T> void createLogWithMessage(Community community, T value, LogType type) {
        saveLog(builderWithCurrentUser(community)
                .logType(type)
                .message(String.valueOf(value)));
    }

    public List<CommunityLogDto> getLogsByGroupname(String groupname, int pageNumber, int pageSize) {
        var community = communityMethodsService.getCommunityByNameOrThrowErr(groupname);
        var membership
                = membershipRepository.findByCommunityAndUser(community, userMethodsService.getCurrentUser())
                .orElseThrow(() -> new UnauthorizedException("User has no permissions"));

        if (community.getType() == CommunityType.DEMOCRACY ||
                (community.getType() == CommunityType.CORPORATE || community.getType() == CommunityType.NEWSPAPER)
                && membership.getRole() != null
        ) {
            var pageable = PageRequest.of(pageNumber, pageSize);
            var list
                    = communityLogRepository.getCommunityLogsByCommunityOrderByIdDesc(community, pageable);
            var dtoList = new ArrayList<CommunityLogDto>();
            list.forEach(log -> dtoList.add(CommunityLogDto.builder()
                    .date(log.getDate())
                    .message(log.getMessage())
                    .logType(log.getLogType())
                    .actionNickname(log.getActionUser().getNickname())
                    .secondNickname(log.getSecondUser() != null ? log.getSecondUser().getNickname() : null)
                    .build()));
            return dtoList;
        }
        throw new UnauthorizedException("User has no permissions");
    }


    public void createLogDeletePost(Community community, UserEntity author, String text) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.DELETE_POST)
                .secondUser(author)
                .message(text));
    }

    public void createLogBanUser(Community community, UserEntity banned) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.BAN_USER)
                .secondUser(banned));
    }
    public void createLogUnbanUser(Community community, UserEntity unbanned) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.UNBAN_USER)
                .secondUser(unbanned));
    }

    public void createLogGrantRole(Community community, UserEntity grantedWithRole, String roleTitle) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.GRANT_ROLE)
                .secondUser(grantedWithRole)
                .message(roleTitle));
    }

    public void createLogRevokeRole(Community community, UserEntity revokedRoleUser, String roleTitle) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.REVOKE_ROLE)
                .secondUser(revokedRoleUser)
                .message(roleTitle));
    }

    public void createLogAcceptJoinRequest(Community community, UserEntity newMember) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.ACCEPT_JOIN_REQUEST)
                .secondUser(newMember));
    }

    public void createLogJoinByInvite(Community community, UserEntity actor, UserEntity joinedUser) {
        saveLog(builder(joinedUser, community)
                .logType(LogType.JOIN_BY_INVITE)
                .secondUser(actor));
    }

    public void createLogNewCommunityTitle(Community community, String newTitle) {
        createLogWithMessage(community, newTitle, LogType.NEW_COMMUNITY_TITLE);
    }
    public void createLogNewGroupname(Community community, String newGroupname) {
        createLogWithMessage(community, newGroupname, LogType.NEW_GROUPNAME);
    }
    public void createLogNewDescription(Community community, String newDescription) {
        createLogWithMessage(community, newDescription, LogType.NEW_DESCRIPTION);
    }

    public void createLogNewCitizenRequirementsDays(Community community, int days) {
        createLogWithMessage(community, days, LogType.NEW_CITIZEN_REQUIREMENTS_DAYS);
    }
    public void createLogNewCitizenRequirementsRating(Community community, int rating) {
        createLogWithMessage(community, rating, LogType.NEW_CITIZEN_REQUIREMENTS_RATING);
    }
    public void createLogNewElectionsPeriod(Community community, int days) {
        createLogWithMessage(community, days, LogType.NEW_ELECTIONS_PERIOD);
    }

    public void createLogRuleAnonPosts(Community community, boolean rule) {
        createLogWithMessage(community, rule, LogType.RULE_ANON_POSTS);
    }
    public void createLogRuleClosedCommunity(Community community, boolean rule) {
        createLogWithMessage(community, rule, LogType.RULE_CLOSED_COMMUNITY);
    }
    public void createLogRuleInviteUsers(Community community, boolean rule) {
        createLogWithMessage(community, rule, LogType.RULE_INVITE_USERS);
    }

    public void createLogUpdateImage(Community community, boolean isDeleted) {
        saveLog(builderWithCurrentUser(community)
                .logType(isDeleted ? LogType.DELETE_IMAGE : LogType.NEW_IMAGE));
    }




}
