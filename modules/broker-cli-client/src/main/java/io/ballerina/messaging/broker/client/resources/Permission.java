package io.ballerina.messaging.broker.client.resources;

import java.util.List;

/**
 * Representation of permission in the broker.
 */
public class Permission {
    private String action;
    private List<String> userGroups;

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
}
