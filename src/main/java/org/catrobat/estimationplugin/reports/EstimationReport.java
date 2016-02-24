package org.catrobat.estimationplugin.reports;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.log4j.Logger;
import org.catrobat.estimationplugin.calc.EstimationCalculator;

import java.util.Map;

public class EstimationReport extends AbstractReport {

    private static final Logger log = Logger.getLogger(EstimationReport.class);
    private final SearchProvider searchProvider;
    private final DateTimeFormatterFactory formatterFactory;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final I18nHelper helper;
    private final PluginSettingsFactory pluginSettingsFactory;

    public EstimationReport(SearchProvider searchProvider, ProjectManager projectManager, I18nHelper helper,
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
        Long numprog = ParameterUtils.getLongParam(params, "numprog");

        EstimationCalculator estimationCalculator = new EstimationCalculator(projectManager, searchProvider, remoteUser);
        Map<String, Object> velocityParams = estimationCalculator.calculateOutputParams(projectId);

        velocityParams.put("projectName", projectManager.getProjectObj(projectId).getName());
        velocityParams.put("filter", "TESTVAL");
        velocityParams.put("countMember", numprog);
        velocityParams.put("comparisonMethod", "STANDARD");
        velocityParams.put("probability", new Float(0));
        velocityParams.put("deviation", new Float(0));

        return descriptor.getHtml("view", velocityParams);
    }

    public void validate(ProjectActionSupport action, Map params) {
        Long numprog = ParameterUtils.getLongParam(params, "numprog");
        Long projectId = ParameterUtils.getLongParam(params, "selectedProjectId");

        if (numprog == null || numprog.longValue() <= 0)
            action.addError("interval", action.getText("estimation-report.interval.invalid"));
        if (projectId == null)
            action.addError("selectedProjectId", action.getText("estimation-report.projectid.invalid"));
    }
}
