package com.example.entity;

import java.io.Serializable;

/**
 * Article summary entity - simplified to one summary length
 */
public class ArticleSummary implements Serializable {
    private Integer id;
    private String model;
    private String title;
    
    // Single summary fields (mid-length, ~50 chars)
    private String summary1;
    private String summary2;
    private String summary3;
    private String summary4;
    private String summary5;
    private String summary6;
    
    // Research-related fields
    private String target;
    private String algorithm1;
    private String algorithm2;
    private String algorithm3;
    private String algorithm4;
    private String environment;
    private String tools;
    private String datas;
    private String standard;
    private String result;
    private String future;
    private String weekpoint;
    private String fullSummary;
    private String keyword;
    private Integer ifteacher;

    // Getters and Setters
    public Integer getIfteacher() {
        return ifteacher;
    }

    public void setIfteacher(Integer ifteacher) {
        this.ifteacher = ifteacher;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getFullSummary() {
        return fullSummary;
    }

    public void setFullSummary(String fullSummary) {
        this.fullSummary = fullSummary;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary1() {
        return summary1;
    }

    public void setSummary1(String summary1) {
        this.summary1 = summary1;
    }

    public String getSummary2() {
        return summary2;
    }

    public void setSummary2(String summary2) {
        this.summary2 = summary2;
    }

    public String getSummary3() {
        return summary3;
    }

    public void setSummary3(String summary3) {
        this.summary3 = summary3;
    }

    public String getSummary4() {
        return summary4;
    }

    public void setSummary4(String summary4) {
        this.summary4 = summary4;
    }

    public String getSummary5() {
        return summary5;
    }

    public void setSummary5(String summary5) {
        this.summary5 = summary5;
    }

    public String getSummary6() {
        return summary6;
    }

    public void setSummary6(String summary6) {
        this.summary6 = summary6;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAlgorithm1() {
        return algorithm1;
    }

    public void setAlgorithm1(String algorithm1) {
        this.algorithm1 = algorithm1;
    }

    public String getAlgorithm2() {
        return algorithm2;
    }

    public void setAlgorithm2(String algorithm2) {
        this.algorithm2 = algorithm2;
    }

    public String getAlgorithm3() {
        return algorithm3;
    }

    public void setAlgorithm3(String algorithm3) {
        this.algorithm3 = algorithm3;
    }

    public String getAlgorithm4() {
        return algorithm4;
    }

    public void setAlgorithm4(String algorithm4) {
        this.algorithm4 = algorithm4;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getTools() {
        return tools;
    }

    public void setTools(String tools) {
        this.tools = tools;
    }

    public String getDatas() {
        return datas;
    }

    public void setDatas(String datas) {
        this.datas = datas;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFuture() {
        return future;
    }

    public void setFuture(String future) {
        this.future = future;
    }

    public String getWeekpoint() {
        return weekpoint;
    }

    public void setWeekpoint(String weekpoint) {
        this.weekpoint = weekpoint;
    }
}