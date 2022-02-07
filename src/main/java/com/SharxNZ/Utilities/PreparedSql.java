package com.SharxNZ.Utilities;

import java.util.ArrayList;
import java.util.HashMap;

public class PreparedSql {

    /**
     * $ -> replace all
     * # -> replace all with lowercase
     * ? -> replace one time and add ''
     * The way the changes are done is by the order you use the `stringChange` commands;
     */

    private String sql;
    private String allValue;
    private final ArrayList<String> stringChanges = new ArrayList<>();

    private void removeInjections(){
        sql = sql.replaceAll("['\"\\\\]", "\\\\$0");
        sql = sql.replaceAll("\n", "\\\\n");
        sql = sql.replaceAll("\n", "\\\\\\\\n");
    }

    public void setSql(String value){
        sql = value;
        removeInjections();
    }

    public void setFormatAll(String value){
        allValue = value;
    }

    public void stringChange(String value){
        this.stringChanges.add(value);
    }

    private String formatSql(String allValue) {
        String tempSql = sql;
        for (String change : stringChanges) {
            tempSql = tempSql.substring(0, tempSql.indexOf('?')) + '\'' + change + '\'' + tempSql.substring(tempSql.indexOf('?') + 1);
        }
        return tempSql.replace("$", allValue).replace("#", allValue.toLowerCase());
    }

    @Override
    public String toString() {
        return formatSql(allValue);
    }
}
