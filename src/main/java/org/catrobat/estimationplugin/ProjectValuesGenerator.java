package org.catrobat.estimationplugin;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProjectValuesGenerator implements ValuesGenerator {

    public Map<String, String> getValues(Map userParams) {
        Map projectValues = new HashMap<String, String>();
        Collection<Project> projects = ComponentAccessor.getProjectManager().getProjectObjects();
        for (Project pr : projects) {
            projectValues.put(pr.getId(), pr.getName());
        }
        return projectValues;
    }
}