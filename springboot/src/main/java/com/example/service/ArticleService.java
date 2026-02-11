package com.example.service;

import com.example.entity.ArticleInfo;
import com.example.entity.ArticleSummary;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

public interface ArticleService {
    List<ArticleInfo> selectArticleList(ArticleInfo articleInfo);
    List<ArticleSummary> selectSummariesByTitle(String title); // Returns a list
    PageInfo<ArticleInfo> selectPage(ArticleInfo articleInfo, Integer pageNum, Integer pageSize);
    void saveArticle(ArticleInfo articleInfo);
    Map<String, String> getFilePathsByTitle(String title);

}