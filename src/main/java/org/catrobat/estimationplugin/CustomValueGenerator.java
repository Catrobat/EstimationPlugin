package org.catrobat.estimationplugin;

import com.atlassian.configurable.ValuesGenerator;

import java.util.HashMap;
import java.util.Map;

public class CustomValueGenerator implements ValuesGenerator {

    public Map<String, String> getValues(Map userParams) {
        Map test = new HashMap<String, String>();
        test.put("1", "one");
        test.put("2", "two");
        return test;
    }
}
