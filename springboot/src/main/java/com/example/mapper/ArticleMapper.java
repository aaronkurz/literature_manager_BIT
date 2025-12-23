// E:\manager\springboot\src\main\java\com\example\mapper\ArticleMapper.java
package com.example.mapper;

import com.example.entity.ArticleInfo;
import com.example.entity.ArticleSummary;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArticleMapper {
    List<ArticleInfo> selectArticleList(ArticleInfo articleInfo);

    @Select("select * from article_summary where title = #{title}")
    List<ArticleSummary> selectSummariesByTitle(String title);

    @Insert("INSERT INTO article_info(srcdatabase, title, author, organ, source, keyword, pubtime, " +
            "firstduty, fund, year, pagecount, clc, url, doi, summary, patha, pathb, pathdocx, pathpdf, pathtxt, " +
            "custom_concept1, custom_concept2, custom_concept3) " +
            "VALUES(#{srcDatabase}, #{title}, #{author}, #{organ}, #{source}, #{keyword}, #{pubTime}, " +
            "#{firstDuty}, #{fund}, #{year}, #{pageCount}, #{clc}, #{url}, #{doi}, #{summary}, #{patha}, #{pathb}, " +
            "#{pathdocx}, #{pathpdf}, #{pathtxt}, #{customConcept1}, #{customConcept2}, #{customConcept3})")
    void insertArticle(ArticleInfo articleInfo);

    // 新增方法：查询文件路径
    @Select("SELECT * FROM article_info WHERE title = #{title}")
    Map<String, String> selectFilePathsByTitle(String title);
}