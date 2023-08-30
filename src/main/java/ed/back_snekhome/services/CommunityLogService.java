package ed.back_snekhome.services;

import ed.back_snekhome.entities.community.Community;
import ed.back_snekhome.entities.community.CommunityLog;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.enums.LogType;
import ed.back_snekhome.repositories.CommunityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommunityLogService {

    private final CommunityLogRepository communityLogRepository;
    private final UserMethodsService userMethodsService;

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
                .message(value + ""));
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
        saveLog(builder(actor, community)
                .logType(LogType.JOIN_BY_INVITE)
                .secondUser(joinedUser));
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
