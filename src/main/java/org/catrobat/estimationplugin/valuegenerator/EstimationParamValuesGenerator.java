package org.catrobat.estimationplugin.valuegenerator;

import com.atlassian.configurable.ValuesGenerator;

import java.util.HashMap;
import java.util.Map;

public class EstimationParamValuesGenerator implements ValuesGenerator{

    public Map<String, String> getValues(Map userParams) {
        Map estimationParams = new HashMap<String, String>();
        estimationParams.put("whole", "whole time");
        estimationParams.put("period", "same period as last year");
        return estimationParams;
    }
}
