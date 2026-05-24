package com.ftn.sbnz.model.models;

public class RuleTemplateParameter {

    private String ruleName;
    private String operator;
    private double percentage;
    private String userGoal;
    private String resultingStatus;
    private String severity;

    public RuleTemplateParameter() {
    }

    public RuleTemplateParameter(String ruleName, String operator, double percentage, String userGoal, String resultingStatus, String severity) {
        this.ruleName = ruleName;
        this.operator = operator;
        this.percentage = percentage;
        this.userGoal = userGoal;
        this.resultingStatus = resultingStatus;
        this.severity = severity;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getUserGoal() {
        return userGoal;
    }

    public void setUserGoal(String userGoal) {
        this.userGoal = userGoal;
    }

    public String getResultingStatus() {
        return resultingStatus;
    }

    public void setResultingStatus(String resultingStatus) {
        this.resultingStatus = resultingStatus;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
