package com.example.utils;

import com.example.entity.ArticleInfo;
import com.example.service.ArticleService;
import com.example.utils.bigmodel.BigModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.example.utils.Caj2pdf.Caj2pdf.runCajToPdf;
import static com.example.utils.neo4jloader.Neo4jLoader.runNeo4jLoader;
import static com.example.utils.pdf2docx.Pdf2docx.runPdfToDocx;
import static com.example.utils.pdf2txt.Pdf2txt.runpdf2txt;
import static com.example.utils.result2mysql.PaperSummarySaver.saveSummary;

/**
 * Post-upload processing - using local Ollama with Ministral 3B
 */
@Component
public class AfterUpload {
    @Autowired
    private ArticleService articleService;
    
    public void file_task(ArticleInfo articleInfo) {
        System.out.println("成功接收上传的文件，提交异步任务,正在处理论文："+articleInfo.getTitle());
        LogUtil_AfterUpload.log("成功接收上传的文件，提交异步任务,正在处理论文："+articleInfo.getTitle());
        
        //获取论文前缀
        String oripath = articleInfo.getPatha().split("\\.")[0];//获取前缀
        String cajpath = oripath + ".caj";
        String pdfpath = oripath + ".pdf";
        String docxpath = oripath + ".docx";
        String txtpath = oripath + ".txt";
        String docpath = oripath + ".doc";
        
        try {
            runCajToPdf();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        if (new File(pdfpath).exists()) {
            articleInfo.setPathpdf(pdfpath);
            System.out.println("pdf已存在或已转换为pdf");
            LogUtil_AfterUpload.log("pdf已存在或已转换为pdf");
        } else {
            System.out.println("pdf转换失败");
            LogUtil_AfterUpload.log("pdf转换失败");
        }

        runPdfToDocx();
        if (new File(docxpath).exists()) {
            articleInfo.setPathdocx(docxpath);
            System.out.println("docx已存在或已转换为docx");
            LogUtil_AfterUpload.log("docx已存在或已转换为docx");
        } else {
            System.out.println("docx转换失败");
            LogUtil_AfterUpload.log("docx转换失败");
        }

        runpdf2txt();
        if (new File(txtpath).exists()) {
            articleInfo.setPathtxt(txtpath);
            System.out.println("已转换为txt");
            LogUtil_AfterUpload.log("已转换为txt");
        } else {
            System.out.println("txt转换失败");
            LogUtil_AfterUpload.log("txt转换失败");
        }

        boolean ifsuccess = true;

        if(!new File(docxpath).exists() && !new File(pdfpath).exists() && !new File(txtpath).exists()) {
            if (new File(cajpath).exists()) new File(cajpath).delete();
            ifsuccess = false;
            System.out.println("没有可用文件，回滚删除已有文件和数据");
            LogUtil_AfterUpload.log("没有可用文件，回滚删除已有文件和数据");
        }

        if(ifsuccess) {
            String s = "你好，按照上面的json串格式返回，要求已经包含在字符串内，必须返回json形式";
            
            if (!new File(cajpath).exists() && !new File(pdfpath).exists()) {
                // Document file upload - use Ollama document understanding
                try {
                    System.out.println("将文档上传给Ollama模型(Mistral 3B),会花几十秒");
                    LogUtil_AfterUpload.log("将文档上传给Ollama模型(Mistral 3B),会花几十秒");
                    String exist = new File(docpath).exists() ? docpath : docxpath;
                    String ollamaRes = BigModelUtil.ollamaDocumentUnderstanding(Config.SUMMARY_JSON + s, exist);
                    saveSummary(Config.OLLAMA_MODEL, articleInfo.getTitle(), ollamaRes, "0");
                    System.out.println("Ollama模型处理完毕,已经结果存入数据库,使用的模型是" + Config.OLLAMA_MODEL);
                    LogUtil_AfterUpload.log("Ollama模型处理完毕,已经结果存入数据库,使用的模型是" + Config.OLLAMA_MODEL);
                } catch (Exception e) {
                    System.out.println("调用失败" + e.getMessage());
                    LogUtil_AfterUpload.log("调用失败" + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // Text upload - use Ollama text generation
                try {
                    String content = new String(Files.readAllBytes(Paths.get(txtpath)));
                    System.out.println("将文本上传给Ollama模型(Mistral 3B),会花几十秒，当前论文字符长度: " + content.length());
                    LogUtil_AfterUpload.log("将文本上传给Ollama模型(Mistral 3B),会花几十秒，当前论文字符长度: " + content.length());
                    
                    String ollamaRes = BigModelUtil.ollamaTextGeneration(content + Config.SUMMARY_JSON + s);
                    saveSummary(Config.OLLAMA_MODEL, articleInfo.getTitle(), ollamaRes, "0");
                    
                    System.out.println("Ollama模型处理完毕,已将结果存入数据库,使用的模型是:" + Config.OLLAMA_MODEL);
                    LogUtil_AfterUpload.log("Ollama模型处理完毕,已将结果存入数据库,使用的模型是:" + Config.OLLAMA_MODEL);
                } catch (IOException e) {
                    System.out.println("调用失败" + e.getMessage());
                    LogUtil_AfterUpload.log("调用失败" + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("调用失败" + e.getMessage());
                    LogUtil_AfterUpload.log("调用失败" + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            
            articleService.saveArticle(articleInfo);
            System.out.println("成功将论文信息存入mysql");
            LogUtil_AfterUpload.log("成功将论文信息存入mysql");
            
            runNeo4jLoader(false, articleInfo.getTitle());
            System.out.println("图谱更新完毕");
            LogUtil_AfterUpload.log("图谱更新完毕");
            System.out.println("----------------------------------------------------");
            LogUtil_AfterUpload.log("----------------------------------------------------");
        }
    }
}