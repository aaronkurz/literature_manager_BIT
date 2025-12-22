<template>
  <div class="upload-container">
    <h2>论文上传</h2>
    <p class="subtitle">上传PDF文件，系统将自动提取元数据和生成摘要</p>
    
    <el-form
        @submit.native.prevent="submitForm"
        label-position="top"
        class="upload-form"
    >
      <el-form-item label="选择PDF文件">
        <el-upload
            :before-upload="handlePaperFile"
            accept=".pdf"
            :limit="1"
            :file-list="paperFileList"
            drag
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将PDF文件拖到此处，或<em>点击上传</em></div>
          <div class="el-upload__tip" slot="tip">仅支持PDF格式，文件大小不超过50MB</div>
        </el-upload>
      </el-form-item>

      <el-form-item class="button-group">
        <el-button type="primary" native-type="submit" :loading="isSubmitting" :disabled="!form.paperFile">
          {{ isSubmitting ? '上传中...' : '上传并处理' }}
        </el-button>
        <el-button type="warning" @click="resetForm">
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <div class="info-box">
      <h3><i class="el-icon-info"></i> 处理流程</h3>
      <ol>
        <li>上传PDF文件</li>
        <li>系统自动提取元数据（标题、作者、摘要等）</li>
        <li>使用本地AI生成论文分析和摘要</li>
        <li>查看并确认结果</li>
        <li>添加到知识图谱</li>
      </ol>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      isSubmitting: false,
      form: {
        paperFile: null,
      },
      paperFileList: []
    };
  },
  methods: {
    handlePaperFile(file) {
      const isLt50M = file.size / 1024 / 1024 < 50;
      if (!isLt50M) {
        this.$message.error('文件大小不能超过 50MB!');
        return false;
      }
      this.form.paperFile = file;
      this.paperFileList = [file];
      return false;
    },
    async submitForm() {
      if (!this.form.paperFile) {
        this.$message.error('请上传PDF文件');
        return;
      }

      this.isSubmitting = true;
      const formData = new FormData();
      formData.append('paperFile', this.form.paperFile);

      try {
        const response = await axios.post('http://localhost:9090/article/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        this.$message.success('文件上传成功，正在处理...');
        
        // Redirect to processing status page with paper title
        const paperTitle = this.form.paperFile.name.replace('.pdf', '');
        this.$router.push({
          name: 'ProcessingStatus',
          params: { title: paperTitle }
        });
        
        this.resetForm();
      } catch (error) {
        console.error('提交失败:', error);
        this.$message.error('提交失败：' + (error.response?.data?.msg || '服务器错误'));
        this.isSubmitting = false;
      }
    },
    resetForm() {
      this.form.paperFile = null;
      this.paperFileList = [];
      this.isSubmitting = false;
    }
  }
};
</script>

<style scoped>
.upload-container {
  max-width: 800px;
  margin: 30px auto;
  padding: 30px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #303133;
  margin-bottom: 10px;
  font-size: 24px;
}

.subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 30px;
  font-size: 14px;
}

.upload-form {
  padding: 0 20px;
}

.button-group {
  text-align: center;
  margin-top: 30px;
}

.el-button {
  padding: 12px 40px;
  margin: 0 15px;
  border-radius: 20px;
  font-size: 16px;
}

.info-box {
  margin-top: 40px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.info-box h3 {
  color: #409EFF;
  margin-bottom: 15px;
  font-size: 16px;
}

.info-box ol {
  margin-left: 20px;
  color: #606266;
}

.info-box li {
  margin-bottom: 8px;
  line-height: 1.6;
}

/deep/ .el-upload-dragger {
  width: 100%;
  height: 200px;
}

@media (max-width: 768px) {
  .upload-container {
    margin: 15px;
    padding: 20px;
  }
  .el-button {
    width: 100%;
    margin: 10px 0;
  }
}
</style>
