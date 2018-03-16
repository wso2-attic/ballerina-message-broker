package io.ballerina.messaging.broker.client.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representation of permission in the broker.
 */
public class Permission {
    private String action;
    private List<String> userGroups = new ArrayList<>();

    public Permission(String action, String group) {
        this.action = action;
        userGroups.add(group);
    }

    public String getAction() {
        return action;
    }

    public List<String> getUserGroups() {
        return userGroups;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
    }

    public String getUserGroupsAsJsonString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"userGroups\": [");

        builder.append(userGroups.stream().map(group -> "\"" + group + "\"")
                .collect(Collectors.joining(",")));

        builder.append("]}");
        return builder.toString();
    }
}
