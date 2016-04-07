package org.catrobat.estimationplugin.calc;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.user.ApplicationUser;
import com.google.gson.Gson;
import org.catrobat.estimationplugin.helper.DateHelper;
import org.catrobat.estimationplugin.jql.IssueListCreator;
import org.catrobat.estimationplugin.misc.FinishedIssueList;

import java.util.*;

public class MonthlyTickets {

    private IssueListCreator issueListCreator;
    private FinishedIssueList finishedIssueListClass;

    private final DateTimeFormatter dateTimeFormatter;

    private List<String> finishedIssuesStatus = new ArrayList<String>();

    private List<String> ticketsPerMonth = new LinkedList<String>();
    private List<String> ticketsPerMonthLabels = new LinkedList<>();
    private List<Long> ticketsPerMonthCount = new LinkedList<>();

    private Date startDate;
    private Date endDate;

    public MonthlyTickets(SearchProvider searchProvider, ApplicationUser user,
                                DateTimeFormatterFactory formatterFactory) {
        issueListCreator = new IssueListCreator(searchProvider, user);
        this.dateTimeFormatter = formatterFactory.formatter().withStyle(DateTimeStyle.ISO_8601_DATE);

        loadSettings();
    }

    private void loadSettings() {
        // TODO: change initialisation to Admin
        finishedIssuesStatus.add("Closed");
    }

    public void calculateTicketsPerMonth(Long projectId, boolean isFilter, Date startDate, Date endDate) throws SearchException {
        Date curStartDate = DateHelper.getStartOfMonth(startDate);
        Date curEndDate = DateHelper.getEndOfMonth(startDate);
        while (curStartDate.getTime() <  endDate.getTime()) {
            long ticketRate = issueListCreator.getMonthlyResolution(projectId, finishedIssuesStatus,
                    isFilter, curStartDate, curEndDate);
            String str = dateTimeFormatter.format(curStartDate);
            str = str.substring(0, str.length()-3);

            ticketsPerMonth.add(dateTimeFormatter.format(curStartDate) + ": " + ticketRate);

            ticketsPerMonthLabels.add(str);
            ticketsPerMonthCount.add(ticketRate);
            curStartDate = DateHelper.getStartOfNextMonth(curStartDate);
            curEndDate = DateHelper.getEndOfNextMonth(curEndDate);
        }
    }

    public Map<String, Object> getTicketsPerMonth(Long projectOrFilterId, boolean isFilter) throws SearchException {
        List<Issue> finishedIssueList = issueListCreator.getIssueListForStatus(projectOrFilterId, finishedIssuesStatus, isFilter);
        finishedIssueListClass = new FinishedIssueList(finishedIssueList);
        Date startDate = finishedIssueListClass.getProjectStartDate();
        Date endDate = new Date();
        if(this.startDate != null) {
            startDate = this.startDate;
        }
        if(this.endDate != null) {
            endDate = this.endDate;
        }

        calculateTicketsPerMonth(projectOrFilterId, isFilter, startDate, endDate);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ticketsPerMonthList", ticketsPerMonth);

        String json = new Gson().toJson(ticketsPerMonthLabels);
        data.put("ticketsPerMonthLabels", json);
        data.put("ticketsPerMonthCount", ticketsPerMonthCount);
        data.put("startdate", startDate);
        data.put("enddate", endDate);
        return data;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
