<template>
  <div class="upload-container">
    <h2>论文信息提交</h2>
    <el-form
        @submit.native.prevent="submitForm"
        label-position="top"
        class="upload-form"
    >
      <!-- 表单内容保持不变 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="支持CAJ/DOC/DOCX/PDF">
            <el-upload
                :before-upload="handlePaperFile"
                accept=".caj,.doc,.docx,.pdf"
                :limit="1"
                :file-list="paperFileList"
            >
              <el-button type="primary" icon="el-icon-upload2">
                点击上传论文文件
              </el-button>
            </el-upload>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="限制数量一个，不限制格式，多个文件请打包上传">
            <el-upload
                :before-upload="handleAttachment"
                multiple
                :file-list="attachmentList"
            >
              <el-button type="primary" icon="el-icon-upload2">
                点击上传附件
              </el-button>
            </el-upload>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="SrcDatabase-来源库">
            <el-input v-model="form.srcDatabase" required placeholder="请输入来源库"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Title-题名">
            <el-input v-model="form.title" required placeholder="请输入论文题目"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Author-作者">
            <el-input v-model="form.author" required placeholder="请输入作者"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Organ-单位">
            <el-input v-model="form.organ" required placeholder="请输入单位"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Source-文献来源">
            <el-input v-model="form.source" required placeholder="请输入文献来源"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Keyword-关键词">
            <el-input v-model="form.keyword" placeholder="请输入关键词"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="PubTime-发表时间">
            <el-date-picker
                v-model="form.pubTime"
                type="datetime"
                required
                placeholder="选择发布日期"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="FirstDuty-第一责任人">
            <el-input v-model="form.firstDuty" placeholder="请输入第一责任人"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Fund-基金">
            <el-input v-model="form.fund" placeholder="请输入基金信息"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Year-年">
            <el-input-number
                v-model="form.year"
                required
                :min="1900"
                :max="2100"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="PageCount-页码">
            <el-input v-model="form.pageCount" placeholder="请输入页码"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="CLC-中图分类号">
            <el-input v-model="form.clc" placeholder="请输入中图分类号"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="URL-网址">
            <el-input v-model="form.url" type="url" placeholder="请输入网址"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="DOI">
            <el-input v-model="form.doi" placeholder="请输入DOI"/>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="Summary-摘要">
            <el-input
                type="textarea"
                v-model="form.summary"
                :rows="4"
                placeholder="请输入摘要"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item class="button-group">
        <el-button type="primary" native-type="submit" :loading="isSubmitting">
          {{ isSubmitting ? '提交中...' : '提交' }}
        </el-button>
        <el-button type="warning" @click="resetForm">
          重置
        </el-button>
      </el-form-item>
    </el-form>
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
        attachment: null,
        srcDatabase: '',
        title: '',
        author: '',
        organ: '',
        source: '',
        keyword: '',
        pubTime: '',
        firstDuty: '',
        fund: '',
        year: new Date().getFullYear(),
        pageCount: null,
        clc: '',
        url: '',
        doi: '',
        summary: ''
      },
      paperFileList: [],
      attachmentList: []
    };
  },
  methods: {
    handlePaperFile(file) {
      this.form.paperFile = file;
      this.paperFileList = [file];
      return false;
    },
    handleAttachment(file) {
      if (!this.form.attachment) this.form.attachment = [];
      this.form.attachment.push(file);
      this.attachmentList.push(file);
      return false;
    },
    async submitForm() {
      this.isSubmitting = true;
      const formData = new FormData();

      // 验证论文文件是否上传
      if (!this.form.paperFile) {
        this.$message.error('请上传论文文件');
        this.isSubmitting = false;
        return;
      }
      formData.append('paperFile', this.form.paperFile);

      // 添加附件（如果有）
      if (this.form.attachment?.length) {
        this.form.attachment.forEach(file => formData.append('attachment', file));
      }

      // 添加表单字段
      Object.entries(this.form).forEach(([key, value]) => {
        if (key !== 'paperFile' && key !== 'attachment') {
          formData.append(key, value || ''); // 确保空值被正确处理
        }
      });

      try {
        await axios.post('http://localhost:9090/article/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        this.$message.success('提交成功！');
        this.resetForm();
      } catch (error) {
        console.error('提交失败:', error);
        this.$message.error('提交失败：' + (error.response?.data?.msg || '服务器错误'));
      } finally {
        this.isSubmitting = false;
      }
    },
    resetForm() {
      Object.assign(this.form, {
        paperFile: null,
        attachment: null,
        srcDatabase: '',
        title: '',
        author: '',
        organ: '',
        source: '',
        keyword: '',
        pubTime: '',
        firstDuty: '',
        fund: '',
        year: new Date().getFullYear(),
        pageCount: null,
        clc: '',
        url: '',
        doi: '',
        summary: ''
      });
      this.paperFileList = [];
      this.attachmentList = [];
    }
  }
};
</script>

<style scoped>
.upload-container {
  max-width: 900px;
  margin: 30px auto;
  padding: 30px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #303133;
  margin-bottom: 30px;
  font-size: 24px;
}

.upload-form {
  padding: 0 20px;
}

.el-upload .el-button {
  width: 100%;
  padding: 12px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-row {
  display: flex;
  flex-wrap: wrap;
}

.el-col {
  display: flex;
  flex-direction: column;
}

.el-input,
.el-input-number,
.el-date-picker,
.el-textarea {
  width: 100%;
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

@media (max-width: 768px) {
  .upload-container {
    margin: 15px;
    padding: 20px;
  }
  .el-col-12,
  .el-col-24 {
    width: 100%;
  }
  .el-button {
    width: 100%;
    margin: 10px 0;
  }
}
</style>