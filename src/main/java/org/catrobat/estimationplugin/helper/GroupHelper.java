package org.catrobat.estimationplugin.helper;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;

import java.util.Collection;

public class GroupHelper {

    public static int getCountOfGroup(String groupName) {
        GroupManager groupManager = ComponentAccessor.getGroupManager();
        Group group = groupManager.getGroup(groupName);
        Collection<User> userCollection = groupManager.getUsersInGroup(group);
        return userCollection.size();
    }

}
