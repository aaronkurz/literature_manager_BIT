<template>
  <div class="processing-container">
    <el-page-header @back="$router.push('/front/home')" content="处理状态">
    </el-page-header>

    <el-card class="status-card" style="margin-top: 20px;">
      <div slot="header">
        <span>{{ title }}</span>
      </div>

      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="文件上传" description="已完成"></el-step>
        <el-step title="格式转换" :description="stepStatus.conversion"></el-step>
        <el-step title="元数据提取" :description="stepStatus.metadata"></el-step>
        <el-step title="AI分析" :description="stepStatus.aiAnalysis"></el-step>
        <el-step title="完成" :description="stepStatus.complete"></el-step>
      </el-steps>

      <div class="status-content" v-if="processing">
        <div class="loading-area">
          <i class="el-icon-loading" style="font-size: 48px; color: #409EFF;"></i>
          <p style="margin-top: 20px; font-size: 16px;">{{ currentMessage }}</p>
        </div>
      </div>

      <div class="result-content" v-if="!processing && metadata">
        <h3>提取的元数据</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ metadata.title }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ metadata.author }}</el-descriptions-item>
          <el-descriptions-item label="单位">{{ metadata.organ }}</el-descriptions-item>
          <el-descriptions-item label="年份">{{ metadata.year }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ metadata.source }}</el-descriptions-item>
          <el-descriptions-item label="关键词">{{ metadata.keyword }}</el-descriptions-item>
          <el-descriptions-item label="DOI" :span="2">{{ metadata.doi }}</el-descriptions-item>
          <el-descriptions-item label="摘要" :span="2">{{ metadata.summary }}</el-descriptions-item>
        </el-descriptions>

        <div class="action-buttons">
          <el-button type="success" @click="approveAndSave">
            <i class="el-icon-check"></i> 确认并保存到数据库
          </el-button>
          <el-button type="danger" @click="reject">
            <i class="el-icon-close"></i> 拒绝并删除
          </el-button>
          <el-button @click="editMetadata">
            <i class="el-icon-edit"></i> 编辑元数据
          </el-button>
        </div>
      </div>

      <div class="error-content" v-if="error">
        <el-alert
          title="处理失败"
          type="error"
          :description="errorMessage"
          show-icon
          :closable="false">
        </el-alert>
        <div class="action-buttons">
          <el-button type="primary" @click="retry">
            <i class="el-icon-refresh"></i> 重试
          </el-button>
          <el-button @click="$router.push('/front/upload')">
            <i class="el-icon-back"></i> 返回上传
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ProcessingStatus',
  data() {
    return {
      title: '',
      processing: true,
      currentStep: 1,
      currentMessage: '正在转换文件格式...',
      stepStatus: {
        conversion: '处理中...',
        metadata: '等待中...',
        aiAnalysis: '等待中...',
        complete: '等待中...'
      },
      metadata: null,
      error: false,
      errorMessage: '',
      pollingInterval: null
    };
  },
  mounted() {
    this.title = this.$route.params.title || '未命名论文';
    this.startPolling();
  },
  beforeDestroy() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
  },
  methods: {
    startPolling() {
      // Poll every 2 seconds for status updates
      this.pollingInterval = setInterval(() => {
        this.checkProcessingStatus();
      }, 2000);
    },
    async checkProcessingStatus() {
      try {
        // Mock progress - in real implementation, call backend API
        const response = await this.$request.get(`/article/processing-status/${this.title}`);
        
        if (response.code === '200') {
          const status = response.data;
          this.updateStatus(status);
          
          if (status.complete) {
            this.processing = false;
            this.metadata = status.metadata;
            clearInterval(this.pollingInterval);
          }
        }
      } catch (error) {
        // Simulate progress for now
        this.simulateProgress();
      }
    },
    simulateProgress() {
      // Temporary simulation until backend status endpoint is implemented
      if (this.currentStep < 4) {
        setTimeout(() => {
          this.currentStep++;
          this.updateMessage();
          if (this.currentStep >= 4) {
            this.processing = false;
            this.mockMetadata();
          }
        }, 3000);
      }
    },
    updateMessage() {
      const messages = [
        '正在转换文件格式...',
        '正在提取元数据...',
        '正在进行AI分析...',
        '处理完成！'
      ];
      this.currentMessage = messages[this.currentStep - 1] || messages[0];
      
      const statuses = ['conversion', 'metadata', 'aiAnalysis', 'complete'];
      if (this.currentStep > 0) {
        this.stepStatus[statuses[this.currentStep - 2]] = '已完成';
      }
      if (this.currentStep <= 4) {
        this.stepStatus[statuses[this.currentStep - 1]] = '处理中...';
      }
    },
    mockMetadata() {
      // Mock metadata - remove this when backend is ready
      this.metadata = {
        title: this.title,
        author: '待提取',
        organ: '待提取',
        year: new Date().getFullYear(),
        source: '待提取',
        keyword: '待提取',
        doi: '待提取',
        summary: '正在使用本地AI模型分析论文内容...'
      };
    },
    updateStatus(status) {
      this.currentStep = status.step;
      this.currentMessage = status.message;
      // Update step statuses
      Object.keys(this.stepStatus).forEach((key, index) => {
        if (index < status.step - 1) {
          this.stepStatus[key] = '已完成';
        } else if (index === status.step - 1) {
          this.stepStatus[key] = '处理中...';
        }
      });
    },
    async approveAndSave() {
      try {
        await this.$request.post('/article/approve', {
          title: this.title,
          metadata: this.metadata
        });
        this.$message.success('已保存到数据库！');
        this.$router.push('/front/home');
      } catch (error) {
        this.$message.error('保存失败：' + error.message);
      }
    },
    async reject() {
      this.$confirm('确认删除此论文吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await this.$request.delete(`/article/${this.title}`);
          this.$message.success('已删除');
          this.$router.push('/front/upload');
        } catch (error) {
          this.$message.error('删除失败：' + error.message);
        }
      });
    },
    editMetadata() {
      // Open metadata edit dialog
      this.$message.info('编辑功能开发中...');
    },
    retry() {
      this.error = false;
      this.processing = true;
      this.currentStep = 1;
      this.startPolling();
    }
  }
};
</script>

<style scoped>
.processing-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
}

.status-card {
  margin-top: 20px;
}

.status-content {
  margin-top: 40px;
}

.loading-area {
  text-align: center;
  padding: 60px 0;
}

.result-content {
  margin-top: 40px;
}

.result-content h3 {
  margin-bottom: 20px;
  color: #303133;
}

.action-buttons {
  margin-top: 30px;
  text-align: center;
}

.action-buttons .el-button {
  margin: 0 10px;
}

.error-content {
  margin-top: 40px;
}

.error-content .el-alert {
  margin-bottom: 20px;
}
</style>
