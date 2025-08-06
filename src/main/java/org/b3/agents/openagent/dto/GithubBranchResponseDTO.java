package org.b3.agents.openagent.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Data
@Getter
@Setter
public class GithubBranchResponseDTO {
    
    private String name;
    private Commit commit;
    @SerializedName("_links")
    private Links links;
    @SerializedName("protected")
    private boolean isProtected;
    private Protection protection;
    @SerializedName("protection_url")
    private String protectionUrl;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public Protection getProtection() {
        return protection;
    }

    public void setProtection(Protection protection) {
        this.protection = protection;
    }

    public String getProtectionUrl() {
        return protectionUrl;
    }

    public void setProtectionUrl(String protectionUrl) {
        this.protectionUrl = protectionUrl;
    }

    public static class Commit {
        private String sha;
        @SerializedName("node_id")
        private String nodeId;
        private CommitDetails commit;
        private String url;
        @SerializedName("html_url")
        private String htmlUrl;
        @SerializedName("comments_url")
        private String commentsUrl;
        private User author;
        private User committer;
        private List<Parent> parents;

        // Getters and Setters
        public String getSha() {
            return sha;
        }

        public void setSha(String sha) {
            this.sha = sha;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public CommitDetails getCommit() {
            return commit;
        }

        public void setCommit(CommitDetails commit) {
            this.commit = commit;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }

        public String getCommentsUrl() {
            return commentsUrl;
        }

        public void setCommentsUrl(String commentsUrl) {
            this.commentsUrl = commentsUrl;
        }

        public User getAuthor() {
            return author;
        }

        public void setAuthor(User author) {
            this.author = author;
        }

        public User getCommitter() {
            return committer;
        }

        public void setCommitter(User committer) {
            this.committer = committer;
        }

        public List<Parent> getParents() {
            return parents;
        }

        public void setParents(List<Parent> parents) {
            this.parents = parents;
        }
    }

    public static class CommitDetails {
        private GitUser author;
        private GitUser committer;
        private String message;
        private Tree tree;
        private String url;
        @SerializedName("comment_count")
        private int commentCount;
        private Verification verification;

        // Getters and Setters
        public GitUser getAuthor() {
            return author;
        }

        public void setAuthor(GitUser author) {
            this.author = author;
        }

        public GitUser getCommitter() {
            return committer;
        }

        public void setCommitter(GitUser committer) {
            this.committer = committer;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Tree getTree() {
            return tree;
        }

        public void setTree(Tree tree) {
            this.tree = tree;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }

        public Verification getVerification() {
            return verification;
        }

        public void setVerification(Verification verification) {
            this.verification = verification;
        }
    }

    public static class GitUser {
        private String name;
        private String email;
        private String date;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    public static class Tree {
        private String sha;
        private String url;

        // Getters and Setters
        public String getSha() {
            return sha;
        }

        public void setSha(String sha) {
            this.sha = sha;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Verification {
        private boolean verified;
        private String reason;
        private String signature;
        private String payload;
        @SerializedName("verified_at")
        private String verifiedAt;

        // Getters and Setters
        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public String getVerifiedAt() {
            return verifiedAt;
        }

        public void setVerifiedAt(String verifiedAt) {
            this.verifiedAt = verifiedAt;
        }
    }

    public static class User {
        private String login;
        private long id;
        @SerializedName("node_id")
        private String nodeId;
        @SerializedName("avatar_url")
        private String avatarUrl;
        @SerializedName("gravatar_id")
        private String gravatarId;
        private String url;
        @SerializedName("html_url")
        private String htmlUrl;
        @SerializedName("followers_url")
        private String followersUrl;
        @SerializedName("following_url")
        private String followingUrl;
        @SerializedName("gists_url")
        private String gistsUrl;
        @SerializedName("starred_url")
        private String starredUrl;
        @SerializedName("subscriptions_url")
        private String subscriptionsUrl;
        @SerializedName("organizations_url")
        private String organizationsUrl;
        @SerializedName("repos_url")
        private String reposUrl;
        @SerializedName("events_url")
        private String eventsUrl;
        @SerializedName("received_events_url")
        private String receivedEventsUrl;
        private String type;
        @SerializedName("user_view_type")
        private String userViewType;
        @SerializedName("site_admin")
        private boolean siteAdmin;

        // Getters and Setters
        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getGravatarId() {
            return gravatarId;
        }

        public void setGravatarId(String gravatarId) {
            this.gravatarId = gravatarId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }

        public String getFollowersUrl() {
            return followersUrl;
        }

        public void setFollowersUrl(String followersUrl) {
            this.followersUrl = followersUrl;
        }

        public String getFollowingUrl() {
            return followingUrl;
        }

        public void setFollowingUrl(String followingUrl) {
            this.followingUrl = followingUrl;
        }

        public String getGistsUrl() {
            return gistsUrl;
        }

        public void setGistsUrl(String gistsUrl) {
            this.gistsUrl = gistsUrl;
        }

        public String getStarredUrl() {
            return starredUrl;
        }

        public void setStarredUrl(String starredUrl) {
            this.starredUrl = starredUrl;
        }

        public String getSubscriptionsUrl() {
            return subscriptionsUrl;
        }

        public void setSubscriptionsUrl(String subscriptionsUrl) {
            this.subscriptionsUrl = subscriptionsUrl;
        }

        public String getOrganizationsUrl() {
            return organizationsUrl;
        }

        public void setOrganizationsUrl(String organizationsUrl) {
            this.organizationsUrl = organizationsUrl;
        }

        public String getReposUrl() {
            return reposUrl;
        }

        public void setReposUrl(String reposUrl) {
            this.reposUrl = reposUrl;
        }

        public String getEventsUrl() {
            return eventsUrl;
        }

        public void setEventsUrl(String eventsUrl) {
            this.eventsUrl = eventsUrl;
        }

        public String getReceivedEventsUrl() {
            return receivedEventsUrl;
        }

        public void setReceivedEventsUrl(String receivedEventsUrl) {
            this.receivedEventsUrl = receivedEventsUrl;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUserViewType() {
            return userViewType;
        }

        public void setUserViewType(String userViewType) {
            this.userViewType = userViewType;
        }

        public boolean isSiteAdmin() {
            return siteAdmin;
        }

        public void setSiteAdmin(boolean siteAdmin) {
            this.siteAdmin = siteAdmin;
        }
    }

    public static class Parent {
        private String sha;
        private String url;
        @SerializedName("html_url")
        private String htmlUrl;

        // Getters and Setters
        public String getSha() {
            return sha;
        }

        public void setSha(String sha) {
            this.sha = sha;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }
    }

    public static class Links {
        private String self;
        private String html;

        // Getters and Setters
        public String getSelf() {
            return self;
        }

        public void setSelf(String self) {
            this.self = self;
        }

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }
    }

    public static class Protection {
        private boolean enabled;
        @SerializedName("required_status_checks")
        private RequiredStatusChecks requiredStatusChecks;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public RequiredStatusChecks getRequiredStatusChecks() {
            return requiredStatusChecks;
        }

        public void setRequiredStatusChecks(RequiredStatusChecks requiredStatusChecks) {
            this.requiredStatusChecks = requiredStatusChecks;
        }
    }

    public static class RequiredStatusChecks {
        @SerializedName("enforcement_level")
        private String enforcementLevel;
        private List<String> contexts;
        private List<String> checks;

        // Getters and Setters
        public String getEnforcementLevel() {
            return enforcementLevel;
        }

        public void setEnforcementLevel(String enforcementLevel) {
            this.enforcementLevel = enforcementLevel;
        }

        public List<String> getContexts() {
            return contexts;
        }

        public void setContexts(List<String> contexts) {
            this.contexts = contexts;
        }

        public List<String> getChecks() {
            return checks;
        }

        public void setChecks(List<String> checks) {
            this.checks = checks;
        }
    }
}
