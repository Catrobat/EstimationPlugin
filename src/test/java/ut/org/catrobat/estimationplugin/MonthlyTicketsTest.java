package ut.org.catrobat.estimationplugin;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import org.catrobat.estimationplugin.calc.MonthlyTickets;
import org.catrobat.estimationplugin.jql.IssueListCreator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class MonthlyTicketsTest {

    private MonthlyTickets monthlyTickets;
    private IssueListCreator issueListCreator;

    private List<String> finishedIssuesStatus = new ArrayList<String>();

    @Before
    public void setUp() {
        finishedIssuesStatus.add("Test");
        monthlyTickets = Mockito.mock(MonthlyTickets.class);
        issueListCreator = Mockito.mock(IssueListCreator.class);
        List<Issue> finishedIssueList = new ArrayList<>();
        //Mockito.when(issueListCreator.getIssueListForStatus(new Long(0), finishedIssuesStatus, false)).thenReturn(finishedIssueList);
    }

    @Test
    public void testCalculateTicketsPerMonth() {
        return;
    }

    @Test
    public void testGetTicketsPerMonth() throws SearchException {
        monthlyTickets.getTicketsPerMonth(new Long(0), false);
    }
}
