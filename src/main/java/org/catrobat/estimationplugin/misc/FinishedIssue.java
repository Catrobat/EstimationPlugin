package org.catrobat.estimationplugin.misc;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class FinishedIssue {

    private Issue issue;

    private Date created;
    private Date workStarted;
    private Date workFinished;
    private long workDuration;

    public FinishedIssue(Issue issue) {
        this.issue = issue;
        try
        {
            extractData();
        }
        catch(Exception e)
        {
           System.out.println(e.getMessage());
        }
    }

    private void extractData() throws Exception {
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

    public Date getCreated() {
        return created;
    }

    public long getWorkDuration() {
        return workDuration;
    }
}
