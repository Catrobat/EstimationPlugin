package org.catrobat.estimationplugin.misc;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;

import java.util.Date;

public class OpenIssue {

    private Issue issue;

    private String estimation;
    private String estimationSML;

    private static CustomField estimationField;
    private static CustomField estimationSMLField;

    static {
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        estimationField = customFieldManager.getCustomFieldObjectByName("Estimated Effort");
        estimationSMLField = customFieldManager.getCustomFieldObjectByName("Effort");
    }

    public OpenIssue(Issue issue) {
        this.issue = issue;
        extractData();

    }

    private void extractData() {
        if (issue.getCustomFieldValue(OpenIssue.estimationField) != null && issue.getCustomFieldValue(OpenIssue.estimationField) instanceof Option) {
            estimation = ((Option) issue.getCustomFieldValue(estimationField)).getValue();
        }
        if (issue.getCustomFieldValue(OpenIssue.estimationSMLField) != null && issue.getCustomFieldValue(OpenIssue.estimationSMLField) instanceof Option) {
            estimationSML = ((Option) issue.getCustomFieldValue(estimationSMLField)).getValue();
        }
    }

    public String getEstimation() {
        return estimation;
    }
}
