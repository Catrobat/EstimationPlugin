package org.catrobat.estimationplugin.misc;

import com.atlassian.jira.issue.Issue;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class FinishedIssueList {

    private List<FinishedIssue> finishedIssueList;

    public FinishedIssueList(List<Issue> issueList) {
        finishedIssueList = new LinkedList<FinishedIssue>();
        for(Issue issue : issueList) {
            FinishedIssue openIssue = new FinishedIssue(issue);
            finishedIssueList.add(openIssue);
        }
    }

    public long getFinishedIssueCount() {
        return finishedIssueList.size();
    }

    public SummaryStatistics getDurationStatistics() {
        SummaryStatistics summaryStatistics = new SummaryStatistics();
        for(FinishedIssue finishedIssue : finishedIssueList) {
            summaryStatistics.addValue(finishedIssue.getWorkDuration());
        }
        return summaryStatistics;
    }

    // TODO: change to helper
    private double convertMillisToDays(double millis) {
        return millis/(1000 * 60 * 60 * 24);
    }

    // TODO: change to be based on date put into backlog
    public long getDaysTicketsWhereOpened() {
        List<FinishedIssue> issues = finishedIssueList;
        ListIterator<FinishedIssue> issueIterator = issues.listIterator();
        long daysOpened = 0;
        while (issueIterator.hasNext()) {
            FinishedIssue currentIssue = issueIterator.next();
            daysOpened += currentIssue.getWorkDuration();
            /*if (created != null && resolved != null) {
                long diffMilliseconds = resolved.getTime() - created.getTime();
                long days = diffMilliseconds / (1000 * 60 * 60 * 24);
                daysOpened += days;
            }*/
        }
        return (long)convertMillisToDays(daysOpened);
    }

    public long getProjectStartInMillis() {
        List<FinishedIssue> issues = finishedIssueList;
        ListIterator<FinishedIssue> issueIterator = issues.listIterator();
        if (!issueIterator.hasNext()) {
            // TODO: make sure you never come here
            return (new Date()).getTime();
        }
        long minCreated = issueIterator.next().getCreated().getTime();
        while (issueIterator.hasNext()) {
            FinishedIssue currentIssue = issueIterator.next();
            long created = currentIssue.getCreated().getTime();
            if (minCreated - created > 0) {
                minCreated = created;
            }
        }
        return minCreated;
    }
}
