package org.catrobat.estimationplugin.misc;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class FinishedIssue {

    private Issue issue;

    public Date getCreated() {
        return created;
    }

    private Date created;
    private Date workStarted;
    private Date workFinished;

    public long getWorkDuration() {
        return workDuration;
    }

    private long workDuration;

    public FinishedIssue(Issue issue) {
        this.issue = issue;
        extractData();
    }

    private void extractData() {
        created = new Date(issue.getCreated().getTime());
        workStarted = new Date(getDatePutIntoBacklog(issue).getTime());
        workFinished = new Date(issue.getResolutionDate().getTime());
        workDuration = workFinished.getTime() - workStarted.getTime();
    }

    private Timestamp getDatePutIntoBacklog(Issue issue) {
        ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
        List<ChangeHistory> issueHistory = changeHistoryManager.getChangeHistories(issue);
        for(ChangeHistory changeHistory : issueHistory) {
            List<GenericValue> changeItem = changeHistory.getChangeItems();
            for(GenericValue genericValue : changeItem) {
                String field = genericValue.getString("field");
                String oldstring = genericValue.getString("oldstring");
                String newstring = genericValue.getString("newstring");
                if (field.equals("status") && oldstring.equals("Issues Pool") && newstring.equals("Backlog")) {
                    Timestamp changedToIssuePool = changeHistory.getTimePerformed();
                    return changedToIssuePool;
                }
            }
        }
        // TODO: this is ugly fix, so items which where never put into backlog, have 0 days worked on
        return issue.getResolutionDate();
    }
}
