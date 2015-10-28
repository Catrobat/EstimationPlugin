package org.catrobat.estimationplugin;

import com.atlassian.configurable.ValuesGenerator;

import java.util.HashMap;
import java.util.Map;

public class EstimationParamValuesGenerator implements ValuesGenerator{

    public Map<String, String> getValues(Map userParams) {
        Map estimationParams = new HashMap<String, String>();
        estimationParams.put("months", "Months");
        estimationParams.put("tickets", "Tickets");
        return estimationParams;
    }
}
