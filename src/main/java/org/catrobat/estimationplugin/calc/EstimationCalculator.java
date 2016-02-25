package org.catrobat.estimationplugin.calc;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.*;


public class EstimationCalculator {

    private final ProjectManager projectManager;
    private final SearchProvider searchProvider;
    private final ApplicationUser user;

    private float ticketsPerDay;
    private float averageTicketDurationDays;

    private List<String> openIssuesStatus = new ArrayList<String>();
    private List<String> finishedIssuesStatus = new ArrayList<String>();
    private CustomField estimationField;
    private CustomField estimationSMLField;
    private long defaultEstimate;

    public EstimationCalculator(ProjectManager projectManager, SearchProvider searchProvider, ApplicationUser user) {
        this.projectManager = projectManager;
        this.searchProvider = searchProvider;
        this.user = user;
        loadSettings();
    }

    private void loadSettings() {
        // TODO: change initialisation to Admin
        openIssuesStatus.add("Backlog");
        openIssuesStatus.add("Open");
        openIssuesStatus.add("In Progress");
        finishedIssuesStatus.add("Closed");
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        estimationField = customFieldManager.getCustomFieldObjectByName("Estimated Effort");
        estimationSMLField = customFieldManager.getCustomFieldObjectByName("Effort");
        defaultEstimate = 5;
    }

    public void calculateTicketsPerDay(Long projectId) throws SearchException
    {
        ticketsPerDay = getFinishedIssueCount(projectId)/((float)getDaysTicketsWhereOpened(projectId));
        averageTicketDurationDays = getDaysTicketsWhereOpened(projectId)/((float)getFinishedIssueCount(projectId));
    }

    public int uncertainty()
    {
        return 7;
    }

    public int calculateBasedOnTotalTime(Long projectid, Date start, Date end, Long interval) {
        Project project = projectManager.getProjectObj(projectid);
        return 0;
    }

    public Map<String, Object> calculateOutputParams(Long projectId) throws SearchException, JqlParseException
    {
        calculateTicketsPerDay(projectId);
        int uncertainty = uncertainty();

        Date today = new Date();
        Calendar epochStart = Calendar.getInstance();
        epochStart.set(1970, 01, 01);
        Date startDate = epochStart.getTime();
        long openIssues = getOpenIssueCount(projectId);
        long openCost = getOpenIssueCost(projectId);
        float daysToFinish = openIssues / ticketsPerDay;
        int daysToFinishRounded = Math.round(daysToFinish);
        Calendar finishDate = Calendar.getInstance();
        finishDate.setTime(today);
        finishDate.add(Calendar.DATE, daysToFinishRounded);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("openIssues", openIssues);
        data.put("openCost", openCost);
        data.put("finishDate", finishDate.getTime());
        data.put("uncertainty", uncertainty);
        String ticketsPerDay = String.valueOf(getFinishedIssueCount(projectId)) +
                "/" +  String.valueOf(getDaysTicketsWhereOpened(projectId));
        data.put("ticketsPerDay", ticketsPerDay);
        data.put("costMap", getOpenIssueCostMap(projectId));
        data.put("smlMap", getOpenIssueSMLMap(projectId));
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        data.put("test", getChangeItemExample(issueManager.getIssueByCurrentKey("WEB-244")));
        data.put("test2", getStringExample(issueManager.getIssueByCurrentKey("WEB-244")));
        data.put("avgDaysOpened", averageTicketDurationDays);
        finishDate.setTime(today);
        finishDate.add(Calendar.DATE,Math.round(averageTicketDurationDays));
        data.put("avgFinishDate", finishDate.getTime());

        return data;
    }

    private List<GenericValue> getChangeItemExample(Issue issue) {
        ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
        List<ChangeHistory> issueHistory = changeHistoryManager.getChangeHistories(issue);
        return issueHistory.get(0).getChangeItems();
    }

    private String getStringExample(Issue issue) {
        ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
        List<ChangeHistory> issueHistory = changeHistoryManager.getChangeHistories(issue);
        List<GenericValue> changeItems = issueHistory.get(0).getChangeItems();
        String teststring = "";
        int count = 0;
        for(GenericValue genericValue : changeItems) {
            String field = genericValue.getString("field");
            teststring += field + " ";
            String oldstring = genericValue.getString("oldstring");
            teststring += oldstring + " ";
            String newstring = genericValue.getString("newstring");
            teststring += newstring + " ";
            count++;
            if (field.equals("status") && oldstring.equals("Issues Pool") && newstring.equals("Backlog")) {
                teststring += "ok";
            }
        }
        teststring += issueHistory.get(0).getTimePerformed().toString();
        return teststring + " " + count;
    }

    private Timestamp getDatePutIntoBacklog(Issue issue) throws SearchException{
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

    // TODO: change to be based on date put into backlog
    private long getDaysTicketsWhereOpened(Long projectId) throws SearchException{
        Query query = getFinishedIssueQuery(projectId);
        SearchResults searchResults = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter());
        List<Issue> issues = searchResults.getIssues();
        ListIterator<Issue> issueIterator = issues.listIterator();
        long daysOpened = 0;
        while (issueIterator.hasNext()) {
            Issue currentIssue = issueIterator.next();
            //Timestamp created = currentIssue.getCreated();
            Timestamp created = getDatePutIntoBacklog(currentIssue);
            Timestamp resolved = currentIssue.getResolutionDate();
            if (created != null && resolved != null) {
                long diffMilliseconds = resolved.getTime() - created.getTime();
                long days = diffMilliseconds / (1000 * 60 * 60 * 24);
                daysOpened += days;
            }
        }
        return daysOpened;
    }

    private long getFinishedIssueCount(Long projectId) throws SearchException{
        Query query = getFinishedIssueQuery(projectId);
        return searchProvider.searchCount(query, user);
    }

    private Query getOpenIssueQuery(Long projectId) {
        return getQueryWithIssueStatus(projectId, openIssuesStatus);
    }

    private Query getFinishedIssueQuery(Long projectId) {
        return getQueryWithIssueStatus(projectId, finishedIssuesStatus);
    }

    private Query getQueryWithIssueStatus(Long projectId, List<String> status) {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();

        if (status.size() == 0) {
            return queryBuilder.buildQuery();
        }

        ListIterator<String> iterator = status.listIterator();
        JqlClauseBuilder clause = queryBuilder.where().project(projectId).and().status().eq(iterator.next());
        while(iterator.hasNext()) {
            clause = clause.or().status().eq(iterator.next());
        }
        Query query = clause.buildQuery();
        return  query;
    }

    private Map<String, Long> getMapForQueryForCustomField(Query query, CustomField customField) throws SearchException {
        SearchResults searchResults = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter());
        List<Issue> issues = searchResults.getIssues();
        ListIterator<Issue> issueIterator = issues.listIterator();
        Map<String, Long> map = new HashMap<String, Long>();
        while (issueIterator.hasNext()) {
            Issue currentIssue = issueIterator.next();
            if (currentIssue.getCustomFieldValue(customField) != null && currentIssue.getCustomFieldValue(customField) instanceof Option) {
                String value = ((Option) currentIssue.getCustomFieldValue(customField)).getValue();
                map.putIfAbsent(value, new Long(0));
                map.put(value, map.get(value) + 1);
            }
        }
        return map;
    }

    private Map<String, Long> getOpenIssueCostMap(Long projectId) throws SearchException {
        Query query = getOpenIssueQuery(projectId);
        return getMapForQueryForCustomField(query, estimationField);
    }

    private Map<String, Long> getOpenIssueSMLMap(Long projectId) throws SearchException {
        Query query = getOpenIssueQuery(projectId);
        return getMapForQueryForCustomField(query, estimationSMLField);
    }

    private long getOpenIssueCost(Long projectId) throws SearchException {
        Map<String, Long> map = getOpenIssueCostMap(projectId);
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

    private long getOpenIssueCount(Long projectId) throws SearchException {
        Query query = getOpenIssueQuery(projectId);
        return searchProvider.searchCount(query, user);
    }
}
