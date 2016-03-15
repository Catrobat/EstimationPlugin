package org.catrobat.estimationplugin.valuegenerator;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.*;

public class WorkflowPoolValuesGenerator implements ValuesGenerator{

    public Map<String, String> getValues(Map userParams) {

        Map pool = new HashMap<String, String>();

        JiraWorkflow catrob = ComponentAccessor.getWorkflowManager().getWorkflow("Catrobat Workflow");
        List<Status> status = catrob.getLinkedStatusObjects();
        ArrayList<String> stat = new ArrayList<String>();
        for(Status st : status)
           pool.put(st.getSimpleStatus().getName(),st.getSimpleStatus().getName());

        return pool;
    }
}
