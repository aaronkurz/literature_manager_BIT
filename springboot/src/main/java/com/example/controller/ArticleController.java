package com.example.controller;

import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import com.example.entity.ArticleInfo;
import com.example.entity.ArticleSummary;
import com.example.entity.ProcessingStatus;
import com.example.service.ArticleService;
import com.example.service.impl.ProcessingStatusService;
import com.example.service.impl.TaskService;
import com.example.utils.AfterUpload;
import com.example.utils.Config;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.utils.neo4jloader.Neo4jLoader.runNeo4jLoader;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private TaskService taskService;
    
    @Resource
    private ProcessingStatusService processingStatusService;

    @Autowired
    private AfterUpload afterUpload;

    @PostMapping("/search")
    public Result<PageInfo<ArticleInfo>> search(@RequestBody ArticleInfo articleInfo,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ArticleInfo> page = articleService.selectPage(articleInfo, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/summary/{title}")
    public Result<List<ArticleSummary>> getSummary(@PathVariable String title) {
        List<ArticleSummary> summaries = articleService.selectSummariesByTitle(title);
        return Result.success(summaries);
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadArticle(@RequestParam("paperFile") MultipartFile paperFile) {
        try {
            // Generate unique task ID
            String taskId = UUID.randomUUID().toString();
            
            // Save file
            String paperFilePath = saveFile(paperFile, "paper");
            
            // Create initial processing status
            ProcessingStatus status = new ProcessingStatus();
            status.setTaskId(taskId);
            status.setFileName(paperFile.getOriginalFilename());
            status.setStatus("UPLOADING");
            status.setProgress(10);
            status.setCurrentStep("文件上传成功");
            status.setFilePath(paperFilePath);
            processingStatusService.createStatus(status);
            
            // Start async processing with taskId
            taskService.executeAsync(() -> afterUpload.processWithStatus(taskId, paperFilePath));
            
            // Return taskId to frontend for status polling
            Map<String, String> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("message", "文件上传成功，正在处理...");
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("500", "文件上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/processing-status/{taskId}")
    public Result<ProcessingStatus> getProcessingStatus(@PathVariable String taskId) {
        ProcessingStatus status = processingStatusService.getStatus(taskId);
        if (status == null) {
            return Result.error("404", "任务不存在");
        }
        return Result.success(status);
    }
    
    @PostMapping("/approve/{taskId}")
    public Result<String> approveAndSave(@PathVariable String taskId, @RequestBody ArticleInfo articleInfo) {
        try {
            ProcessingStatus status = processingStatusService.getStatus(taskId);
            if (status == null) {
                return Result.error("404", "任务不存在");
            }
            
            if (!"PENDING_APPROVAL".equals(status.getStatus())) {
                return Result.error("400", "任务状态不正确");
            }
            
            // Set file path from status
            articleInfo.setPatha(status.getFilePath());
            
            // Save article and summary to database
            afterUpload.saveApprovedArticle(articleInfo, status);
            
            // Update status to approved
            status.setStatus("APPROVED");
            status.setProgress(100);
            status.setCurrentStep("已批准并保存到数据库");
            processingStatusService.updateStatus(status);
            processingStatusService.markCompleted(taskId);
            
            return Result.success("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "保存失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/reject/{taskId}")
    public Result<String> rejectAndDelete(@PathVariable String taskId) {
        try {
            ProcessingStatus status = processingStatusService.getStatus(taskId);
            if (status == null) {
                return Result.error("404", "任务不存在");
            }
            
            // Delete uploaded file
            if (status.getFilePath() != null) {
                FileUtil.del(status.getFilePath());
            }
            
            // Update status to rejected
            status.setStatus("REJECTED");
            status.setCurrentStep("用户拒绝，已删除文件");
            processingStatusService.updateStatus(status);
            
            return Result.success("已拒绝并删除");
        } catch (Exception e) {
            return Result.error("500", "操作失败：" + e.getMessage());
        }
    }

    private String saveFile(MultipartFile file, String type) throws IOException {
        String uploadPath = Config.UPLOAD_PATH;
        if (!FileUtil.isDirectory(uploadPath)) {
            FileUtil.mkdir(uploadPath);
        }
        String originalFilename = file.getOriginalFilename();
        String ext = FileUtil.getSuffix(originalFilename);
        String fileName = type + "_" + UUID.randomUUID() + "." + ext;
        String filePath = uploadPath + "/" + fileName;
        FileUtil.writeBytes(file.getBytes(), filePath);
        return filePath;
    }

    @GetMapping("/file-paths/{title}")
    public Result<Map<String, String>> getFilePaths(@PathVariable String title) {
        Map<String, String> filePaths = articleService.getFilePathsByTitle(title);
        return Result.success(filePaths);
    }

    @GetMapping("/download/{title}/{field}")
    public void downloadFile(@PathVariable String title, @PathVariable String field, HttpServletResponse response) {
        try {
            Map<String, String> filePaths = articleService.getFilePathsByTitle(title);
            String filePath = filePaths.get(field);
            if (filePath == null || !FileUtil.exist(filePath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String fileName = title + "_" + field + "." + FileUtil.getSuffix(filePath);
            response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            response.setContentType("application/octet-stream");

            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            OutputStream os = response.getOutputStream();
            os.write(fileBytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    @PostMapping("/rebuild")
    public Result<String> rebuildGraph() {
        try {
            runNeo4jLoader(true, "前面为true时后面的字符串失效");
            return Result.success("图谱重建成功");
        } catch (Exception e) {
            return Result.error("500", "图谱重建失败：" + e.getMessage());
        }
    }
}