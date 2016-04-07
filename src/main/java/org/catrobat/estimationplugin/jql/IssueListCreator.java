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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class IssueListCreator {


    private final SearchProvider searchProvider;
    private final ApplicationUser user;

    private List<Query> queryLog = new LinkedList<Query>();

    public IssueListCreator(SearchProvider searchProvider, ApplicationUser user) {
        this.searchProvider = searchProvider;
        this.user = user;
    }

    private Query getQueryWithIssueStatus(Long projectOrFilterId, List<String> status, boolean isFilter) {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();

        if (status.size() == 0) {
            return queryBuilder.buildQuery();
        }

        ListIterator<String> iterator = status.listIterator();
        JqlClauseBuilder clause = queryBuilder.where();
        if (!isFilter) {
            clause = clause.project(projectOrFilterId);
        } else {
            clause = clause.savedFilter().eq(projectOrFilterId);
        }
        clause = clause.and().sub().status().eq(iterator.next());
        while(iterator.hasNext()) {
            clause = clause.or().status().eq(iterator.next());
        }
        Query query = clause.endsub().buildQuery();
        queryLog.add(query);
        return  query;
    }

    private Query getQueryWithIssueStatusResolvedBetween(Long projectOrFilterId, List<String> status, boolean isFilter, Date startDate, Date endDate) {
        Query query = getQueryWithIssueStatus(projectOrFilterId, status, isFilter);

        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(query);
        queryBuilder.where().and().resolutionDateBetween(startDate, endDate);
        query = queryBuilder.buildQuery();
        queryLog.add(query);
        return query;
    }

    public long getMonthlyResolution(Long projectOrFilterId, List<String> status, boolean isFilter, Date startDate, Date endDate) throws SearchException {
        Query query = getQueryWithIssueStatusResolvedBetween(projectOrFilterId, status, isFilter, startDate, endDate);
        return searchProvider.searchCount(query, user);
    }

    public List<Issue> getIssueListForStatus(Long projectOrFilterId, List<String> status, boolean isFilter) throws SearchException {
        Query query = getQueryWithIssueStatus(projectOrFilterId, status, isFilter);
        SearchResults searchResults = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter());
        List<Issue> issueList = searchResults.getIssues();
        return  issueList;
    }

    public String getQueryLog() {
        return queryLog.toString();
    }
}
