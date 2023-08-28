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

    public void createLogNewCommunityTitle(Community community, String newTitle) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.NEW_COMMUNITY_TITLE)
                .message(newTitle));
    }

    public void createLogNewGroupname(Community community, String newGroupname) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.NEW_GROUPNAME)
                .message(newGroupname));
    }

    public void createLogNewDescription(Community community, String newDescription) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.NEW_DESCRIPTION)
                .message(newDescription));
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

    public void createLogNewCitizenRequirementsDays(Community community, int days) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.NEW_CITIZEN_REQUIREMENTS_DAYS)
                .message(days + ""));
    }

    public void createLogNewCitizenRequirementsRating(Community community, int rating) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.NEW_CITIZEN_REQUIREMENTS_RATING)
                .message(rating + ""));
    }

    public void createLogNewElectionsPeriod(Community community, int days) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.NEW_ELECTIONS_PERIOD)
                .message(days + ""));
    }

    public void createLogRuleAnonPosts(Community community, boolean rule) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.RULE_ANON_POSTS)
                .message(rule + ""));
    }

    public void createLogRuleClosedCommunity(Community community, boolean rule) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.RULE_CLOSED_COMMUNITY)
                .message(rule + ""));
    }

    public void createLogRuleInviteUsers(Community community, boolean rule) {
        saveLog(builderWithCurrentUser(community)
                .logType(LogType.RULE_INVITE_USERS)
                .message(rule + ""));
    }



}
