<template>
  <div class="upload-container">
    <h2>Paper Upload</h2>
    <p class="subtitle">Upload a PDF file and the system will automatically extract metadata and generate a summary</p>
    
    <el-form
        @submit.native.prevent="submitForm"
        label-position="top"
        class="upload-form"
    >
      <el-form-item label="Select PDF File">
        <el-upload
            :before-upload="handlePaperFile"
            accept=".pdf"
            :limit="1"
            :file-list="paperFileList"
            drag
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">Drag PDF file here, or <em>click to upload</em></div>
          <div class="el-upload__tip" slot="tip">Only PDF format, max 50MB</div>
        </el-upload>
      </el-form-item>

      <el-form-item class="button-group">
        <el-button type="primary" native-type="submit" :loading="isSubmitting" :disabled="!form.paperFile">
          {{ isSubmitting ? 'Uploading & Processing...' : 'Upload & Process' }}
        </el-button>
        <el-button type="warning" @click="resetForm" :disabled="isSubmitting">
          Reset
        </el-button>
      </el-form-item>
    </el-form>

    <div class="info-box">
      <h3><i class="el-icon-info"></i> Processing Workflow</h3>
      <ol>
        <li>Upload PDF file</li>
        <li>Automatic format conversion (PDF→TXT)</li>
        <li>Extract metadata using local AI (title, author, abstract, etc.)</li>
        <li>Generate paper analysis and summary</li>
        <li>Review and confirm extracted information</li>
        <li>Automatically add to knowledge graph after approval</li>
      </ol>
      <p class="note">Note: Processing may take 1-2 minutes. Please confirm extracted information on the review page before adding to the database</p>
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
        this.$message.error('File size must not exceed 50MB!');
        return false;
      }
      this.form.paperFile = file;
      this.paperFileList = [file];
      return false;
    },
    async submitForm() {
      if (!this.form.paperFile) {
        this.$message.error('Please upload a PDF file');
        return;
      }

      this.isSubmitting = true;
      const formData = new FormData();
      formData.append('paperFile', this.form.paperFile);

      try {
        const response = await axios.post('http://localhost:9090/article/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        if (response.data.code === '200') {
          const taskId = response.data.data.taskId;
          this.$message.success('File uploaded successfully, processing...');
          
          // Redirect to processing status page
          this.$router.push({
            name: 'ProcessingStatus',
            params: { taskId: taskId }
          });
        } else {
          this.$message.error('Upload failed: ' + response.data.msg);
          this.isSubmitting = false;
        }
      } catch (error) {
        console.error('Submission failed:', error);
        this.$message.error('Submission failed: ' + (error.response?.data?.msg || 'Server error'));
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

.info-box .note {
  margin-top: 15px;
  padding: 10px;
  background: #fff3cd;
  border-left: 3px solid #ffc107;
  color: #856404;
  font-size: 14px;
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
