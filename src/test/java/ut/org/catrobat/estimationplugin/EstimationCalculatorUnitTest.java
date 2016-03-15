package ut.org.catrobat.estimationplugin;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import org.catrobat.estimationplugin.calc.EstimationCalculator;
import org.catrobat.estimationplugin.jql.IssueListCreator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class EstimationCalculatorUnitTest {

    private EstimationCalculator calc;

    private ProjectManager manager;
    private SearchProvider provider;
    private ApplicationUser user;
    private DateTimeFormatterFactory date;
    private DateTimeFormatter formater;

    private CustomField estimationField;
    private CustomField estimationSMLField;

    private IssueListCreator issueListCreator;
    private List<Issue> openIssueList;
    private List<Issue> finishedIssueList;

    private CustomFieldManager custmanager;

/*    @Before
    public void setup()
    {
        manager = mock(ProjectManager.class);
        provider = mock(SearchProvider.class);
        user = mock(ApplicationUser.class);
        date = mock(DateTimeFormatterFactory.class);
        estimationField = mock(CustomField.class);
        estimationSMLField = mock(CustomField.class);
        issueListCreator = mock(IssueListCreator.class);
        formater = mock(DateTimeFormatter.class);

        CustomFieldManager custmanager = mock(CustomFieldManager.class);
        calc = new EstimationCalculator(manager,provider,user,date);
    }

    @Test
    public void test()
    {
        assertEquals("load settings failed", 3, (long)(calc.getOpenIssuesStatus().size()));
    }*/
}
