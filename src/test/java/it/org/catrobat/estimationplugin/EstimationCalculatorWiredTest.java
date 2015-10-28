package it.org.catrobat.estimationplugin;

import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.runner.RunWith;

@RunWith(AtlassianPluginsTestRunner.class)
public class EstimationCalculatorWiredTest {

    private final ProjectManager projectManager;
    private final SearchProvider searchProvider;
    private final ApplicationUser user;


    public EstimationCalculatorWiredTest(ProjectManager projectManager, SearchProvider searchProvider,
                                ApplicationUser user)
    {
        this.projectManager = projectManager;
        this.searchProvider = searchProvider;
        this.user = user;
    }
}
