package com.f1.ami.web.centermanger.editor;

import java.util.*;
import java.util.regex.*;

public class ParseAttributes {
    public static void main(String[] args) {
        String x = "type=\"RIGHT\" selects=\"Currency=FX.Currency,Name=FXDelta.Name,PnL=FX.DeltaTMinusOne*FXDelta.Delta*100.0\" on=\"FX.Currency == FXDelta.Currency\"";

        Map<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile("(\\w+)=\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(x);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            result.put(key, value);
        }

        // Print the result
        for (Map.Entry<String, String> entry : result.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}