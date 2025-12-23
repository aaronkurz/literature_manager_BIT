package com.example.entity;

import java.io.Serializable;

/**
 * 文章信息实体类
 */
public class ArticleInfo implements Serializable {
    private Integer id;
    private String srcDatabase;
    private String title;
    private String author;
    private String organ;
    private String source;
    private String keyword;
    private String summary;
    private String pubTime;
    private String firstDuty;
    private String fund;
    private String year;
    private String pageCount;
    private String clc;
    private String url;
    private String doi;
    private String patha;
    private String pathb;
    private String pathdocx;
    private String pathtxt;
    private String pathpdf;
    
    // Custom concepts extracted during processing
    // Each field stores JSON: {"relationshipName": "method", "matchingConcepts": ["RCT", "Cohort"]}
    private String customConcept1;
    private String customConcept2;
    private String customConcept3;

    public String getPathdocx() {
        return pathdocx;
    }

    public void setPathdocx(String pathdocx) {
        this.pathdocx = pathdocx;
    }

    public String getPathtxt() {
        return pathtxt;
    }

    public void setPathtxt(String pathtxt) {
        this.pathtxt = pathtxt;
    }

    public String getPathpdf() {
        return pathpdf;
    }

    public void setPathpdf(String pathpdf) {
        this.pathpdf = pathpdf;
    }

    public String getPatha() {
        return patha;
    }

    public void setPatha(String patha) {
        this.patha = patha;
    }

    public String getPathb() {
        return pathb;
    }

    public void setPathb(String pathb) {
        this.pathb = pathb;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSrcDatabase() {
        return srcDatabase;
    }

    public void setSrcDatabase(String srcDatabase) {
        this.srcDatabase = srcDatabase;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getOrgan() {
        return organ;
    }

    public void setOrgan(String organ) {
        this.organ = organ;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPubTime() {
        return pubTime;
    }

    public void setPubTime(String pubTime) {
        this.pubTime = pubTime;
    }

    public String getFirstDuty() {
        return firstDuty;
    }

    public void setFirstDuty(String firstDuty) {
        this.firstDuty = firstDuty;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPageCount() {
        return pageCount;
    }

    public void setPageCount(String pageCount) {
        this.pageCount = pageCount;
    }

    public String getClc() {
        return clc;
    }

    public void setClc(String clc) {
        this.clc = clc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }
    
    public String getCustomConcept1() {
        return customConcept1;
    }
    
    public void setCustomConcept1(String customConcept1) {
        this.customConcept1 = customConcept1;
    }
    
    public String getCustomConcept2() {
        return customConcept2;
    }
    
    public void setCustomConcept2(String customConcept2) {
        this.customConcept2 = customConcept2;
    }
    
    public String getCustomConcept3() {
        return customConcept3;
    }
    
    public void setCustomConcept3(String customConcept3) {
        this.customConcept3 = customConcept3;
    }
}