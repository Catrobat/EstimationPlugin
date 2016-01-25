package org.catrobat.estimationplugin;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.query.Query;

import java.util.*;


public class EstimationCalculator {

    private final ProjectManager projectManager;
    private final SearchProvider searchProvider;
    private final ApplicationUser user;
    private long maxCount = 0;

    private Collection<Date> dates = new ArrayList<Date>();
    private Collection<Long> openIssueCounts = new ArrayList<Long>();

    public EstimationCalculator(ProjectManager projectManager, SearchProvider searchProvider, ApplicationUser user) {
        this.projectManager = projectManager;
        this.searchProvider = searchProvider;
        this.user = user;
    }

    public int calculateBasedOnTotalTime(Long projectid, Date start, Date end, Long interval) {
        Project project = projectManager.getProjectObj(projectid);
        return 0;
    }

    public Map<String, Object> getDataOfCalculation(Date startDate, Date endDate, Long interval, Long projectId)
            throws SearchException {
        Map<String, Object> data = new HashMap<String, Object>();
        getIssueCount(startDate, endDate, interval, projectId);
        data.put("openIssueCounts", openIssueCounts);
        data.put("dates", dates);
        data.put("macCount", maxCount);
        return data;
    }

    private long getOpenIssueCount(Date startDate, Date endDate, Long projectId) throws SearchException {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        Query query = queryBuilder.where().createdBetween(startDate, endDate).and().project(projectId).buildQuery();
        return searchProvider.searchCount(query, user);
    }

    public void getIssueCount(Date startDate, Date endDate, Long interval, Long projectId) throws SearchException {
        long intervalValue = interval.longValue() * DateUtils.DAY_MILLIS;
        Date newStartDate;
        long count = 0;
        while (startDate.before(endDate)) {
            newStartDate = new Date(startDate.getTime() + intervalValue);
            if (newStartDate.after(endDate))
                count = getOpenIssueCount(startDate, endDate, projectId);
            else
                count = getOpenIssueCount(startDate, newStartDate, projectId);
            if (maxCount < count)
                maxCount = count;
            openIssueCounts.add(new Long(count));
            dates.add(startDate);
            startDate = newStartDate;
        }
    }
}
