package org.catrobat.estimationplugin.calc;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
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

import java.sql.Timestamp;
import java.util.*;


public class EstimationCalculator {

    private final ProjectManager projectManager;
    private final SearchProvider searchProvider;
    private final ApplicationUser user;

    private float ticketsPerDay;

    private List<String> openIssuesStatus = new ArrayList<String>();
    private List<String> finishedIssuesStatus = new ArrayList<String>();
    private CustomField estimationField;

    public EstimationCalculator(ProjectManager projectManager, SearchProvider searchProvider, ApplicationUser user) {
        this.projectManager = projectManager;
        this.searchProvider = searchProvider;
        this.user = user;
        // TODO: change initialisation to Admin
        openIssuesStatus.add("Open");
        openIssuesStatus.add("In Progress");
        finishedIssuesStatus.add("Closed");
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        estimationField = customFieldManager.getCustomFieldObjectByName("Estimated Effort");
    }

    public void calculateTicketsPerDay(Long projectId) throws SearchException
    {
        ticketsPerDay = getFinishedIssueCount(projectId)/((float)getDaysTicketsWhereOpened(projectId));
    }

    public int uncertainty()
    {
        return 2;
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

        return data;
    }

    private long getDaysTicketsWhereOpened(Long projectId) throws SearchException{
        Query query = getFinishedIssueQuery(projectId);
        SearchResults searchResults = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter());
        List<Issue> issues = searchResults.getIssues();
        ListIterator<Issue> issueIterator = issues.listIterator();
        long daysOpened = 0;
        while (issueIterator.hasNext()) {
            Issue currentIssue = issueIterator.next();
            Timestamp created = currentIssue.getCreated();
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

    private long getOpenIssueCost(Long projectId) throws SearchException {
        Query query = getOpenIssueQuery(projectId);
        //Query query = queryBuilder.where().createdBetween(startDate, endDate).and().project(projectId).buildQuery();
        //Collector<>
        //JqlQueryParser queryParser = ComponentAccessor.getComponent(JqlQueryParser.class);
        //Query query = queryParser.parseQuery("status=Backlog OR status=\"In Development\"");
        //TODO maybe implement with collector
        SearchResults searchResults = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter());
        List<Issue> issues = searchResults.getIssues();
        ListIterator<Issue> issueIterator = issues.listIterator();
        long sumEstimates = 0;
        while (issueIterator.hasNext()) {
            Issue currentIssue = issueIterator.next();
            if (currentIssue.getEstimate() != null) {
                // TODO: check type
                if (currentIssue.getCustomFieldValue(estimationField) != null && currentIssue.getCustomFieldValue(estimationField) instanceof Double) {
                    Double test = (Double) currentIssue.getCustomFieldValue(estimationField);
                    long estimate = Math.round(test);
                    sumEstimates += estimate;
                }
            }
        }
        return sumEstimates;
    }

    private long getOpenIssueCount(Long projectId) throws SearchException {
        Query query = getOpenIssueQuery(projectId);
        return searchProvider.searchCount(query, user);
    }
}
