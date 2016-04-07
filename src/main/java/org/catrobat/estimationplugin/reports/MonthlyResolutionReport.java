package org.catrobat.estimationplugin.reports;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.log4j.Logger;
import org.catrobat.estimationplugin.calc.EstimationCalculator;

import java.util.Map;

public class MonthlyResolutionReport extends AbstractReport {

    private static final Logger log = Logger.getLogger(EstimationReport.class);
    private final SearchProvider searchProvider;
    private final DateTimeFormatterFactory formatterFactory;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final I18nHelper helper;
    private final PluginSettingsFactory pluginSettingsFactory;

    public MonthlyResolutionReport(SearchProvider searchProvider, ProjectManager projectManager, I18nHelper helper,
                            IssueManager issueManager, DateTimeFormatterFactory formatterFactory,
                            PluginSettingsFactory pluginSettingsFactory) {
        this.searchProvider = searchProvider;
        this.projectManager = projectManager;
        this.helper = helper;
        this.issueManager = issueManager;
        this.formatterFactory = formatterFactory;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception {
        ApplicationUser remoteUser = action.getLoggedInApplicationUser();
        Long projectId = ParameterUtils.getLongParam(params, "selectedProjectId");


        EstimationCalculator estimationCalculator = new EstimationCalculator(projectManager, searchProvider, remoteUser, formatterFactory);
        Map<String, Object> velocityParams;
        velocityParams = estimationCalculator.getTicketsPerMonth(projectId, false);

        return descriptor.getHtml("view", velocityParams);
    }
}
