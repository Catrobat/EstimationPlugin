package it.org.catrobat.estimationplugin;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.runner.RunWith;

@RunWith(AtlassianPluginsTestRunner.class)
public class EstimationCalculatorWiredTest {

    private final ProjectManager projectManager;
    private final SearchService searchService;
    private final ApplicationUser user;


    public EstimationCalculatorWiredTest(ProjectManager projectManager, SearchService searchService,
                                ApplicationUser user)
    {
        this.projectManager = projectManager;
        this.searchService = searchService;
        this.user = user;
    }
}
