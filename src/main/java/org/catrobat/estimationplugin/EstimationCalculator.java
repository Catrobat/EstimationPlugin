package org.catrobat.estimationplugin;

import com.atlassian.jira.issue.Issue;
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

import java.util.*;


public class EstimationCalculator {

    private final ProjectManager projectManager;
    private final SearchProvider searchProvider;
    private final ApplicationUser user;
    private long maxCount = 0;

    private Collection<Date> dates = new ArrayList<Date>();
    private Collection<Long> openIssueCounts = new ArrayList<Long>();

    private float ticketsPerDay;

    private List<String> issuesToBeFinished = new ArrayList<String>();

    public EstimationCalculator(ProjectManager projectManager, SearchProvider searchProvider, ApplicationUser user) {
        this.projectManager = projectManager;
        this.searchProvider = searchProvider;
        this.user = user;
        // TODO: change initialisation to Admin
        issuesToBeFinished.add("Open");
        issuesToBeFinished.add("In Progress");
    }

    public void calculateTicketsPerDay()
    {
        ticketsPerDay = 0.2f;
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
        calculateTicketsPerDay();
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

        return data;
    }

    private Query getOpenIssueQuery(Long projectId) {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();

        if (issuesToBeFinished.size() == 0) {
            return queryBuilder.buildQuery();
        }

        ListIterator<String> iterator = issuesToBeFinished.listIterator();
        JqlClauseBuilder clause = queryBuilder.where().project(projectId).and().status().eq(iterator.next());
        while(iterator.hasNext()) {
            clause = clause.or().status().eq(iterator.next());
        }
        Query query = clause.buildQuery();
        return  query;
    }

    private long getOpenIssueCost(Long projectId) throws JqlParseException, SearchException {
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
                //Change to custom field
                //currentIssue.getCustomFieldValue("")
                sumEstimates += currentIssue.getEstimate();
            }
        }
        return sumEstimates;
    }

    private long getOpenIssueCount(Long projectId) throws SearchException {
        Query query = getOpenIssueQuery(projectId);
        return searchProvider.searchCount(query, user);
    }
}
