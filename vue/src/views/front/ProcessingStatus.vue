<template>
  <div class="processing-container">
    <div class="header">
      <h2>论文处理状态</h2>
      <p class="filename">{{ status.fileName }}</p>
    </div>

    <!-- Progress Steps -->
    <el-steps :active="currentStepIndex" finish-status="success" align-center class="steps">
      <el-step title="上传文件" description="文件已上传"></el-step>
      <el-step title="格式转换" description="转换为PDF/TXT"></el-step>
      <el-step title="提取元数据" description="使用AI提取信息"></el-step>
      <el-step title="等待审核" description="确认信息"></el-step>
    </el-steps>

    <!-- Progress Bar -->
    <div class="progress-section">
      <el-progress 
        :percentage="status.progress" 
        :status="progressStatus"
        :stroke-width="20"
      ></el-progress>
      <p class="current-step">{{ status.currentStep }}</p>
    </div>

    <!-- Error Message -->
    <el-alert
      v-if="status.status === 'FAILED'"
      title="处理失败"
      type="error"
      :description="status.errorMessage"
      :closable="false"
      show-icon
      style="margin: 20px 0"
    ></el-alert>

    <!-- Metadata Review (when pending approval) -->
    <div v-if="status.status === 'PENDING_APPROVAL'" class="metadata-section">
      <h3><i class="el-icon-document"></i> 提取的元数据</h3>
      <el-form label-position="left" label-width="100px" class="metadata-form">
        <el-form-item label="标题">
          <el-input v-model="editableMetadata.title"></el-input>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="editableMetadata.author"></el-input>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="editableMetadata.organ"></el-input>
        </el-form-item>
        <el-form-item label="年份">
          <el-input v-model="editableMetadata.year"></el-input>
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="editableMetadata.source"></el-input>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="editableMetadata.keyword" type="textarea" :rows="2"></el-input>
        </el-form-item>
        <el-form-item label="DOI">
          <el-input v-model="editableMetadata.doi"></el-input>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="editableMetadata.summary" type="textarea" :rows="6"></el-input>
        </el-form-item>
      </el-form>

      <!-- Custom Concepts Section -->
      <div v-if="hasCustomConcepts" class="custom-concepts-section">
        <h3><i class="el-icon-star-on"></i> 自定义概念识别</h3>
        <el-card 
          v-for="(concept, index) in customConcepts" 
          :key="index" 
          class="concept-card"
          shadow="hover"
        >
          <div class="concept-header">
            <strong>{{ concept.relationshipName }}</strong>
          </div>
          <div class="concept-values">
            <el-tag 
              v-for="(value, vIndex) in concept.matchingConcepts" 
              :key="vIndex"
              type="success"
              size="medium"
            >{{ value }}</el-tag>
            <span v-if="concept.matchingConcepts.length === 0" class="no-match">未识别到匹配概念</span>
          </div>
        </el-card>
      </div>

      <div class="action-buttons">
        <el-button type="success" size="large" @click="approve" :loading="isApproving">
          <i class="el-icon-check"></i> 批准并添加到数据库
        </el-button>
        <el-button type="danger" size="large" @click="reject" :loading="isRejecting">
          <i class="el-icon-close"></i> 拒绝并删除
        </el-button>
      </div>
    </div>

    <!-- Action buttons for other states -->
    <div v-if="status.status === 'FAILED'" class="action-buttons">
      <el-button type="primary" @click="goBack">返回上传页面</el-button>
    </div>

    <div v-if="status.status === 'APPROVED'" class="success-message">
      <i class="el-icon-success"></i>
      <h3>处理成功！</h3>
      <p>论文已成功添加到数据库和知识图谱</p>
      <el-button type="primary" @click="goToSearch">查看论文</el-button>
      <el-button @click="goBack">继续上传</el-button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      taskId: '',
      status: {
        fileName: '',
        status: 'UPLOADING',
        progress: 0,
        currentStep: '正在上传...',
        errorMessage: '',
        extractedTitle: '',
        extractedAuthors: '',
        extractedInstitution: '',
        extractedYear: '',
        extractedSource: '',
        extractedKeywords: '',
        extractedDoi: '',
        extractedAbstract: '',
        extractedSummary: '',
        extractedCustomConcept1: '',
        extractedCustomConcept2: '',
        extractedCustomConcept3: ''
      },
      editableMetadata: {
        title: '',
        author: '',
        organ: '',
        year: '',
        source: '',
        keyword: '',
        doi: '',
        summary: ''
      },
      customConcepts: [],
      pollingInterval: null,
      isApproving: false,
      isRejecting: false
    };
  },
  computed: {
    currentStepIndex() {
      const statusMap = {
        'UPLOADING': 0,
        'CONVERTING': 1,
        'EXTRACTING': 2,
        'PENDING_APPROVAL': 3,
        'APPROVED': 4,
        'REJECTED': 0,
        'FAILED': 0
      };
      return statusMap[this.status.status] || 0;
    },
    progressStatus() {
      if (this.status.status === 'FAILED') return 'exception';
      if (this.status.status === 'APPROVED') return 'success';
      return null;
    },
    hasCustomConcepts() {
      return this.customConcepts.length > 0;
    }
  },
  mounted() {
    this.taskId = this.$route.params.taskId;
    if (!this.taskId) {
      this.$message.error('缺少任务ID');
      this.$router.push('/front/upload');
      return;
    }
    this.startPolling();
  },
  beforeDestroy() {
    this.stopPolling();
  },
  methods: {
    startPolling() {
      this.fetchStatus();
      this.pollingInterval = setInterval(() => {
        this.fetchStatus();
      }, 2000); // Poll every 2 seconds
    },
    stopPolling() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval);
        this.pollingInterval = null;
      }
    },
    async fetchStatus() {
      try {
        const response = await axios.get(`http://localhost:9090/article/processing-status/${this.taskId}`);
        if (response.data.code === '200') {
          this.status = response.data.data;
          
          // Update editable metadata when pending approval
          if (this.status.status === 'PENDING_APPROVAL') {
            this.editableMetadata = {
              title: this.status.extractedTitle || '',
              author: this.status.extractedAuthors || '',
              organ: this.status.extractedInstitution || '',
              year: this.status.extractedYear || '',
              source: this.status.extractedSource || '',
              keyword: this.status.extractedKeywords || '',
              doi: this.status.extractedDoi || '',
              summary: this.status.extractedAbstract || ''
            };
            
            // Parse custom concepts
            this.customConcepts = [];
            for (let i = 1; i <= 3; i++) {
              const conceptKey = `extractedCustomConcept${i}`;
              const conceptJson = this.status[conceptKey];
              if (conceptJson) {
                try {
                  const concept = JSON.parse(conceptJson);
                  if (concept.relationshipName) {
                    this.customConcepts.push({
                      relationshipName: concept.relationshipName,
                      matchingConcepts: concept.matchingConcepts || []
                    });
                  }
                } catch (e) {
                  console.error('解析自定义概念失败:', e);
                }
              }
            }
            
            this.stopPolling(); // Stop polling when waiting for user action
          }
          
          // Stop polling if processing is complete
          if (this.status.status === 'APPROVED' || this.status.status === 'FAILED' || this.status.status === 'REJECTED') {
            this.stopPolling();
          }
        }
      } catch (error) {
        console.error('获取状态失败:', error);
        this.$message.error('获取处理状态失败');
      }
    },
    async approve() {
      this.isApproving = true;
      try {
        // Prepare article info with user-edited metadata
        const articleInfo = {
          title: this.editableMetadata.title,
          author: this.editableMetadata.author,
          organ: this.editableMetadata.organ,
          year: this.editableMetadata.year,
          source: this.editableMetadata.source,
          keyword: this.editableMetadata.keyword,
          doi: this.editableMetadata.doi,
          summary: this.editableMetadata.summary,
          // Include custom concepts from status
          customConcept1: this.status.extractedCustomConcept1,
          customConcept2: this.status.extractedCustomConcept2,
          customConcept3: this.status.extractedCustomConcept3
        };
        
        const response = await axios.post(`http://localhost:9090/article/approve/${this.taskId}`, articleInfo);
        if (response.data.code === '200') {
          this.$message.success('论文已成功添加到数据库！');
          this.status.status = 'APPROVED';
          this.status.progress = 100;
        } else {
          this.$message.error('保存失败：' + response.data.msg);
        }
      } catch (error) {
        console.error('批准失败:', error);
        this.$message.error('批准失败：' + (error.response?.data?.msg || '服务器错误'));
      } finally {
        this.isApproving = false;
      }
    },
    async reject() {
      this.$confirm('确定要拒绝并删除这篇论文吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.isRejecting = true;
        try {
          const response = await axios.post(`http://localhost:9090/article/reject/${this.taskId}`);
          if (response.data.code === '200') {
            this.$message.success('已拒绝并删除文件');
            setTimeout(() => {
              this.$router.push('/front/upload');
            }, 1500);
          } else {
            this.$message.error('操作失败：' + response.data.msg);
          }
        } catch (error) {
          console.error('拒绝失败:', error);
          this.$message.error('操作失败：' + (error.response?.data?.msg || '服务器错误'));
        } finally {
          this.isRejecting = false;
        }
      }).catch(() => {
        // User cancelled
      });
    },
    goBack() {
      this.$router.push('/front/upload');
    },
    goToSearch() {
      this.$router.push('/front/home');
    }
  }
};
</script>

<style scoped>
.processing-container {
  max-width: 1000px;
  margin: 30px auto;
  padding: 30px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h2 {
  color: #303133;
  margin-bottom: 10px;
}

.filename {
  color: #909399;
  font-size: 14px;
}

.steps {
  margin-bottom: 40px;
}

.progress-section {
  margin: 40px 0;
}

.current-step {
  text-align: center;
  margin-top: 15px;
  font-size: 16px;
  color: #606266;
  font-weight: 500;
}

.metadata-section {
  margin-top: 40px;
  padding: 30px;
  background: #f5f7fa;
  border-radius: 8px;
}

.metadata-section h3 {
  color: #409EFF;
  margin-bottom: 20px;
  font-size: 18px;
}

.metadata-form {
  margin-bottom: 20px;
}

.custom-concepts-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 2px dashed #e0e0e0;
}

.custom-concepts-section h3 {
  color: #67C23A;
  margin-bottom: 20px;
  font-size: 18px;
}

.concept-card {
  margin-bottom: 15px;
}

.concept-header {
  font-size: 16px;
  color: #303133;
  margin-bottom: 10px;
}

.concept-values {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.concept-values .el-tag {
  font-size: 14px;
  padding: 6px 12px;
}

.no-match {
  color: #909399;
  font-size: 14px;
  font-style: italic;
}

.action-buttons {
  text-align: center;
  margin-top: 30px;
}

.action-buttons .el-button {
  margin: 0 15px;
  padding: 15px 40px;
  font-size: 16px;
}

.success-message {
  text-align: center;
  padding: 60px 20px;
}

.success-message i {
  font-size: 80px;
  color: #67C23A;
  margin-bottom: 20px;
}

.success-message h3 {
  color: #303133;
  margin-bottom: 10px;
}

.success-message p {
  color: #909399;
  margin-bottom: 30px;
}

@media (max-width: 768px) {
  .processing-container {
    margin: 15px;
    padding: 20px;
  }
  
  .action-buttons .el-button {
    display: block;
    width: 100%;
    margin: 10px 0;
  }
}
</style>