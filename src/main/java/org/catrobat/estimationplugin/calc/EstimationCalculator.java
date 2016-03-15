package org.catrobat.estimationplugin.calc;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import org.catrobat.estimationplugin.helper.DateHelper;
import org.catrobat.estimationplugin.jql.IssueListCreator;
import org.catrobat.estimationplugin.misc.FinishedIssueList;
import org.catrobat.estimationplugin.misc.OpenIssueList;

import java.util.*;


public class EstimationCalculator {

    private final ProjectManager projectManager;
    private final DateTimeFormatter dateTimeFormatter;

    private double ticketsPerDay;
    List<String> ticketsPerMonth = new LinkedList<String>();
    private double averageTicketDurationDays;

    private List<String> openIssuesStatus = new ArrayList<String>();
    private List<String> finishedIssuesStatus = new ArrayList<String>();
    private CustomField estimationField;
    private CustomField estimationSMLField;

    private IssueListCreator issueListCreator;

    private FinishedIssueList finishedIssueListClass;
    private OpenIssueList openIssueListClass;

    private Map<String, Long> costMap; //debug only
    private Map<String, Long> smlMap; //debug only

    public EstimationCalculator(ProjectManager projectManager, SearchProvider searchProvider, ApplicationUser user,
                                DateTimeFormatterFactory formatterFactory) {
        this.projectManager = projectManager;
        issueListCreator = new IssueListCreator(searchProvider, user);
        this.dateTimeFormatter = formatterFactory.formatter().withStyle(DateTimeStyle.ISO_8601_DATE);

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
    }

    public void calculateTicketsPerMonth(Long projectId, boolean isFilter, Date startDate) throws SearchException {
        Date today = new Date();
        Date curStartDate = DateHelper.getStartOfMonth(startDate);
        Date curEndDate = DateHelper.getEndOfMonth(startDate);
        while (curStartDate.getTime() <  today.getTime()) {
            long ticketRate = issueListCreator.getMonthlyResolution(projectId, finishedIssuesStatus,
                    isFilter, curStartDate, curEndDate);
            ticketsPerMonth.add(dateTimeFormatter.format(curStartDate) + ": " + ticketRate);
            curStartDate = DateHelper.getStartOfNextMonth(curStartDate);
            curEndDate = DateHelper.getEndOfNextMonth(curEndDate);
        }
    }

    public void calculateTicketsPerDay() {
        ticketsPerDay = finishedIssueListClass.getFinishedIssueCount()/((double)finishedIssueListClass.getProjectDurationFromStart());
        averageTicketDurationDays = finishedIssueListClass.getDaysTicketsWhereOpened()/((double)finishedIssueListClass.getFinishedIssueCount());
    }

    public Map<String, Object> prepareMap() {
        calculateTicketsPerDay();

        Date today = new Date();
        long openIssues = openIssueListClass.getOpenIssueCount();
        long openCost = openIssueListClass.getOpenIssueCost();
        double daysToFinish = openIssues / ticketsPerDay;
        long daysToFinishRounded = Math.round(daysToFinish);
        Calendar finishDate = Calendar.getInstance();
        finishDate.setTime(today);
        finishDate.add(Calendar.DATE, (int)daysToFinishRounded);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("openIssues", openIssues);
        data.put("openCost", openCost);
        data.put("finishDate", dateTimeFormatter.format(finishDate.getTime()));
        data.put("uncertainty", DateHelper.convertMillisToDays((long)finishedIssueListClass.getDurationStatistics().getStandardDeviation())/7);
        String ticketsPerDay = String.valueOf(finishedIssueListClass.getFinishedIssueCount()) +
                "/" +  String.valueOf(finishedIssueListClass.getDaysTicketsWhereOpened() + "/" + String.valueOf(finishedIssueListClass.getProjectDurationFromStart()));
        data.put("ticketsPerDay", ticketsPerDay);
        data.put("costMap", costMap);
        data.put("smlMap", smlMap);
        data.put("avgDaysOpened", averageTicketDurationDays);
        data.put("avgDaysOpenedNew", DateHelper.convertMillisToDays((long)finishedIssueListClass.getDurationStatistics().getMean()));
        finishDate.setTime(today);
        finishDate.add(Calendar.DATE,(int)Math.round(averageTicketDurationDays));
        data.put("avgFinishDate", dateTimeFormatter.format(finishDate.getTime()));
        data.put("projectStart", dateTimeFormatter.format(finishedIssueListClass.getProjectStartDate()));
        data.put("openIssueList", openIssueListClass.getOpenIssueList());
        data.put("queryLog", issueListCreator.getQueryLog());

        data.put("ticketRateMonthly", ticketsPerMonth.toString());

        return data;
    }

    public Map<String, Object> calculateOutputParams(Long projectOrFilterId, boolean isFilter) throws SearchException {
        List<Issue> openIssueList = issueListCreator.getIssueListForStatus(projectOrFilterId, openIssuesStatus, isFilter);
        List<Issue> finishedIssueList = issueListCreator.getIssueListForStatus(projectOrFilterId, finishedIssuesStatus, isFilter);
        finishedIssueListClass = new FinishedIssueList(finishedIssueList);
        openIssueListClass = new OpenIssueList(openIssueList);
        costMap = getMapOfEffortsFromIssueListForCustomField(openIssueList, estimationField);
        smlMap = getMapOfEffortsFromIssueListForCustomField(openIssueList, estimationSMLField);

        calculateTicketsPerMonth(projectOrFilterId, isFilter, finishedIssueListClass.getProjectStartDate());

        return prepareMap();
    }

    private Map<String, Long> getMapOfEffortsFromIssueListForCustomField(List<Issue> issueList, CustomField customField) {
        ListIterator<Issue> issueIterator = issueList.listIterator();
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

    public List<String> getOpenIssuesStatus() {
        return openIssuesStatus;
    }

    public void setOpenIssuesStatus(List<String> selected_open_issue_status)
    {
        openIssuesStatus.clear();
        openIssuesStatus = selected_open_issue_status;
    }
}
