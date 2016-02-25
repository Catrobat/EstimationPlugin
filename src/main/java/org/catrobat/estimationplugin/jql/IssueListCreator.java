package org.catrobat.estimationplugin.jql;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

import java.util.List;
import java.util.ListIterator;

public class IssueListCreator {


    private final SearchProvider searchProvider;
    private final ApplicationUser user;

    public IssueListCreator(SearchProvider searchProvider, ApplicationUser user) {
        this.searchProvider = searchProvider;
        this.user = user;
    }

    public Query getQueryWithIssueStatus(Long projectId, List<String> status) {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();

        if (status.size() == 0) {
            return queryBuilder.buildQuery();
        }

        ListIterator<String> iterator = status.listIterator();
        JqlClauseBuilder clause = queryBuilder.where().project(projectId).and().status().eq(iterator.next());
        while(iterator.hasNext()) {
            clause = clause.or().status().eq(iterator.next());
        }
        Query query = clause.buildQuery();
        return  query;
    }

    public List<Issue> getIssueListForStatus(Long projectId, List<String> status) throws SearchException {
        Query query = getQueryWithIssueStatus(projectId, status);
        SearchResults searchResults = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter());
        List<Issue> issueList = searchResults.getIssues();
        return  issueList;
    }
}
