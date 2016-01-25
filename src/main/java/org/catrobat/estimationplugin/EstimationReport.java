package org.catrobat.estimationplugin;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstimationReport extends AbstractReport {

    private static final Logger log = Logger.getLogger(EstimationReport.class);
    private static final int MAX_HEIGHT = 200;
    private Long DEFAULT_INTERVAL = new Long(7);
    private final SearchProvider searchProvider;
    private final OutlookDateManager outlookDateManager;
    private final DateTimeFormatterFactory formatterFactory;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final I18nHelper helper;

    public EstimationReport(SearchProvider searchProvider, OutlookDateManager outlookDateManager,
                            ProjectManager projectManager, I18nHelper helper, IssueManager issueManager,
                            DateTimeFormatterFactory formatterFactory) {
        this.searchProvider = searchProvider;
        this.outlookDateManager = outlookDateManager;
        this.projectManager = projectManager;
        this.helper = helper;
        this.issueManager = issueManager;
        this.formatterFactory = formatterFactory;
    }

    public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception {
        DateTimeFormatter formatter = formatterFactory.formatter().forLoggedInUser().withStyle(DateTimeStyle.DATE_PICKER);
        ApplicationUser remoteUser = action.getLoggedInApplicationUser();
        I18nHelper i18nBean = helper;
        Long projectId = ParameterUtils.getLongParam(params, "selectedProjectId");
        Date startDate = formatter.parse(ParameterUtils.getStringParam(params, "startDate"));
        Date endDate = formatter.parse(ParameterUtils.getStringParam(params, "endDate"));
        Long interval = ParameterUtils.getLongParam(params, "interval");
        if (interval == null || interval.longValue() <= 0) {
            interval = DEFAULT_INTERVAL;
            log.error(action.getText("estimation-report.default.interval"));
        }
        EstimationCalculator estimationCalculator = new EstimationCalculator(projectManager, searchProvider, remoteUser);
        Map<String, Object> data = estimationCalculator.getDataOfCalculation(startDate, endDate, interval, projectId);
        long maxCount = (Long)data.get("macCount");
        Collection<Date> dates = (Collection<Date>)data.get("dates");
        Collection<Long> openIssueCounts = (Collection<Long>)data.get("openIssueCounts");
        List<Number> normalCount = new ArrayList<Number>();
        if (maxCount != MAX_HEIGHT && maxCount > 0) {
            for (Long asLong : openIssueCounts) {
                Float floatValue = new Float((asLong.floatValue() / maxCount) * MAX_HEIGHT);
                // Round it back to an integer
                Integer newValue = new Integer(floatValue.intValue());
                normalCount.add(newValue);
            }
        }
        if (maxCount < 0)
            action.addErrorMessage(action.getText("estimation-report.error"));
        Map<String, Object> velocityParams = new HashMap<String, Object>();
        velocityParams.put("startDate", startDate);
        velocityParams.put("endDate", endDate);
        velocityParams.put("openCount", openIssueCounts);
        velocityParams.put("normalisedCount", normalCount);
        velocityParams.put("dates", dates);
        velocityParams.put("maxHeight", new Integer(MAX_HEIGHT));
        velocityParams.put("outlookDate", outlookDateManager.getOutlookDate(i18nBean.getLocale()));
        velocityParams.put("projectName", projectManager.getProjectObj(projectId).getName());
        velocityParams.put("interval", interval);
        long issue = issueManager.getIssueCountForProject(projectId);
        return descriptor.getHtml("view", velocityParams);
    }

    public void validate(ProjectActionSupport action, Map params) {
        I18nHelper i18nBean = helper;
        Date startDate = ParameterUtils.getDateParam(params, "startDate", i18nBean.getLocale());
        Date endDate = ParameterUtils.getDateParam(params, "endDate", i18nBean.getLocale());
        Long interval = ParameterUtils.getLongParam(params, "interval");
        Long projectId = ParameterUtils.getLongParam(params, "selectedProjectId");
        OutlookDate outlookDate = outlookDateManager.getOutlookDate(i18nBean.getLocale());
        if (startDate == null || !outlookDate.isDatePickerDate(outlookDate.formatDMY(startDate)))
            action.addError("startDate", action.getText("estimation-report.startdate.required"));
        if (endDate == null || !outlookDate.isDatePickerDate(outlookDate.formatDMY(endDate)))
            action.addError("endDate", action.getText("estimation-report.enddate.required"));
        if (interval == null || interval.longValue() <= 0)
            action.addError("interval", action.getText("estimation-report.interval.invalid"));
        if (projectId == null)
            action.addError("selectedProjectId", action.getText("estimation-report.projectid.invalid"));
        if (startDate != null && endDate != null && endDate.before(startDate)) {
            action.addError("endDate", action.getText("estimation-report.before.startdate"));
        }
    }
}
