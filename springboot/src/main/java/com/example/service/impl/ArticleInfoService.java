// E:\manager\springboot\src\main\java\com\example\service\ArticleInfoService.java
package com.example.service.impl;

import cn.hutool.core.io.FileUtil;
import com.example.entity.ArticleInfo;
import com.example.mapper.ArticleInfoMapper;
import com.example.utils.Config; // Config class defining UPLOAD_PATH
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@Service
public class ArticleInfoService {

    @Resource
    private ArticleInfoMapper articleInfoMapper;

    private static final Logger log = LoggerFactory.getLogger(ArticleInfoService.class);

    public void add(ArticleInfo articleInfo) {
        articleInfoMapper.insert(articleInfo);
    }

    @Transactional
    public void deleteById(Integer id) {
        ArticleInfo articleInfo = articleInfoMapper.selectById(id);
        if (articleInfo != null) {
            // Delete local files
            deleteLocalFiles(articleInfo);

            // Execute original delete logic
            String title = articleInfo.getTitle();
            articleInfoMapper.deleteById(id);
            articleInfoMapper.deleteSummaryByTitle(title);
        }
    }

    /**
     * Delete article-related local files
     */
    private void deleteLocalFiles(ArticleInfo articleInfo) {
        // Get base upload path
        String basePath = Config.UPLOAD_PATH; // Base path defined in Config
        if (!basePath.endsWith(File.separator)) {
            basePath += File.separator;
        }

        // Delete patha file (main file)
        if (articleInfo.getPatha() != null && !articleInfo.getPatha().isEmpty()) {
            String fullPath = resolveFullPath(basePath, articleInfo.getPatha());
            if (FileUtil.del(fullPath)) {
                log.info("Deleted file: {}", fullPath);
            } else {
                log.warn("Failed to delete file: {}", fullPath);
            }
        }

        // Delete pathb files (attachments, may be semicolon-separated)
        if (articleInfo.getPathb() != null && !articleInfo.getPathb().isEmpty()) {
            String[] attachmentPaths = articleInfo.getPathb().split(";");
            for (String path : attachmentPaths) {
                if (!path.isEmpty()) {
                    String fullPath = resolveFullPath(basePath, path);
                    if (FileUtil.del(fullPath)) {
                        log.info("Deleted attachment: {}", fullPath);
                    } else {
                        log.warn("Failed to delete attachment: {}", fullPath);
                    }
                }
            }
        }

        // Delete pathdocx file
        if (articleInfo.getPathdocx() != null && !articleInfo.getPathdocx().isEmpty()) {
            String fullPath = resolveFullPath(basePath, articleInfo.getPathdocx());
            if (FileUtil.del(fullPath)) {
                log.info("Deleted docx file: {}", fullPath);
            } else {
                log.warn("Failed to delete docx file: {}", fullPath);
            }
        }

        // Delete pathtxt file
        if (articleInfo.getPathtxt() != null && !articleInfo.getPathtxt().isEmpty()) {
            String fullPath = resolveFullPath(basePath, articleInfo.getPathtxt());
            if (FileUtil.del(fullPath)) {
                log.info("Deleted txt file: {}", fullPath);
            } else {
                log.warn("Failed to delete txt file: {}", fullPath);
            }
        }

        // Delete pathpdf file
        if (articleInfo.getPathpdf() != null && !articleInfo.getPathpdf().isEmpty()) {
            String fullPath = resolveFullPath(basePath, articleInfo.getPathpdf());
            if (FileUtil.del(fullPath)) {
                log.info("Deleted pdf file: {}", fullPath);
            } else {
                log.warn("Failed to delete pdf file: {}", fullPath);
            }
        }
    }

    /**
     * Resolve full file path
     */
    private String resolveFullPath(String basePath, String relativePath) {
        // Return if already absolute
        if (FileUtil.isAbsolutePath(relativePath)) {
            return relativePath;
        }
        // Otherwise, join base path and relative path
        return basePath + relativePath.replace("/", File.separator);
    }

    @Transactional
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            deleteById(id);
        }
    }

    public void updateById(ArticleInfo articleInfo) {
        articleInfoMapper.updateById(articleInfo);
    }

    public ArticleInfo selectById(Integer id) {
        return articleInfoMapper.selectById(id);
    }

    public List<ArticleInfo> selectAll(ArticleInfo articleInfo) {
        return articleInfoMapper.selectAll(articleInfo);
    }

    public PageInfo<ArticleInfo> selectPage(ArticleInfo articleInfo, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ArticleInfo> list = articleInfoMapper.selectAll(articleInfo);
        return PageInfo.of(list);
    }
}