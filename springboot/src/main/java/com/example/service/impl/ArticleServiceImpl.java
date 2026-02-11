package com.example.service.impl;

import com.example.entity.ArticleInfo;
import com.example.entity.ArticleSummary;
import com.example.mapper.ArticleMapper;
import com.example.service.ArticleService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Override
    public List<ArticleInfo> selectArticleList(ArticleInfo articleInfo) {
        return articleMapper.selectArticleList(articleInfo);
    }

    @Override
    public List<ArticleSummary> selectSummariesByTitle(String title) {
        return articleMapper.selectSummariesByTitle(title); // Returns a list
    }

    @Override
    public PageInfo<ArticleInfo> selectPage(ArticleInfo articleInfo, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ArticleInfo> list = articleMapper.selectArticleList(articleInfo);
        return PageInfo.of(list);
    }

    @Override
    @Transactional
    public void saveArticle(ArticleInfo articleInfo) {
        articleMapper.insertArticle(articleInfo);
    }

    @Override
    public Map<String, String> getFilePathsByTitle(String title) {
        return articleMapper.selectFilePathsByTitle(title);
    }
}