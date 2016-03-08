package org.catrobat.estimationplugin.misc;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;

import java.util.*;

public class OpenIssueList {

    private List<OpenIssue> openIssueList;

    private long defaultEstimate;

    public OpenIssueList(List<Issue> issueList) {
        openIssueList = new LinkedList<OpenIssue>();
        for(Issue issue : issueList) {
            OpenIssue openIssue = new OpenIssue(issue);
            openIssueList.add(openIssue);
        }

        defaultEstimate = 5;
    }

    public List<OpenIssue> getOpenIssueList() {
        return openIssueList;
    }

    public long getOpenIssueCount() {
        return openIssueList.size();
    }

    private Map<String, Long> getMapOfEffortsFromIssueListForCustomField(List<OpenIssue> issueList) {
        ListIterator<OpenIssue> issueIterator = issueList.listIterator();
        Map<String, Long> map = new HashMap<String, Long>();
        while (issueIterator.hasNext()) {
            OpenIssue currentIssue = issueIterator.next();
            String value = currentIssue.getEstimation();
            map.putIfAbsent(value, new Long(0));
            map.put(value, map.get(value) + 1);
        }
        return map;
    }

    public long getOpenIssueCost() {
        Map<String, Long> map = getMapOfEffortsFromIssueListForCustomField(openIssueList);
        long sumEstimates = 0;
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            long key;
            try {
                key = Long.parseLong(entry.getKey());
            }catch(NumberFormatException nfe) {
                key = defaultEstimate;
            }
            sumEstimates += key * entry.getValue();
        }
        return sumEstimates;
    }
}
