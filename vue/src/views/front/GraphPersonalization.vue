<template>
  <div class="personalization-container">
    <div class="header">
      <h2>图谱个性化设置</h2>
      <p class="description">自定义论文概念关系，最多3个组合，每个组合最多5个概念</p>
    </div>

    <el-card v-for="order in [1, 2, 3]" :key="order" class="concept-card" shadow="hover">
      <div slot="header" class="card-header">
        <span>概念组合 {{ order }}</span>
        <el-button 
          v-if="concepts[order - 1] && concepts[order - 1].relationshipName"
          type="danger" 
          size="small" 
          @click="deleteConcept(order)"
          icon="el-icon-delete"
        >删除</el-button>
      </div>

      <el-form :model="concepts[order - 1]" label-width="120px">
        <el-form-item label="关系名称">
          <el-input 
            v-model="concepts[order - 1].relationshipName" 
            placeholder="例如: method, dataset, evaluation_metric"
            :maxlength="50"
          ></el-input>
          <div class="tip">语义上有意义的关系名称，用于描述论文与概念之间的联系</div>
        </el-form-item>

        <el-form-item label="概念列表">
          <div class="concepts-input">
            <el-tag
              v-for="(concept, index) in concepts[order - 1].conceptsList"
              :key="index"
              closable
              @close="removeConcept(order, index)"
              class="concept-tag"
            >{{ concept }}</el-tag>
            
            <el-input
              v-if="showConceptInput[order - 1]"
              v-model="newConcept[order - 1]"
              ref="conceptInput"
              size="small"
              class="input-new-concept"
              @keyup.enter.native="addConcept(order)"
              @blur="addConcept(order)"
              placeholder="输入概念名称"
              :maxlength="100"
            ></el-input>
            
            <el-button
              v-else
              size="small"
              @click="showInputForConcept(order)"
              :disabled="concepts[order - 1].conceptsList.length >= 5"
              class="button-new-concept"
            >+ 添加概念</el-button>
          </div>
          <div class="tip">
            最多5个概念。例如: RCT, Retrospective_Cohort, Prospective_Cohort, Mendelian_Randomization_Study
          </div>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            @click="saveConcept(order)"
            :disabled="!canSave(order)"
            :loading="saving[order - 1]"
          >保存概念组合</el-button>
          <span v-if="concepts[order - 1].id" class="save-tip">
            <i class="el-icon-success"></i> 已保存
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="info-section">
      <el-alert type="info" :closable="false">
        <h4>使用说明</h4>
        <ul>
          <li><strong>关系名称</strong>：描述论文与概念之间的关系类型（如 method、dataset、evaluation_metric）</li>
          <li><strong>概念列表</strong>：可能适用于论文的具体概念值（如 RCT、Cohort Study）</li>
          <li>在上传论文时，AI会自动识别论文中是否使用了这些概念</li>
          <li>在知识图谱中可以按照自定义概念进行过滤和查询</li>
          <li>概念名称建议使用英文或英文缩写，避免使用特殊字符</li>
        </ul>
      </el-alert>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'GraphPersonalization',
  data() {
    return {
      concepts: [
        { id: null, relationshipName: '', conceptsList: [], displayOrder: 1 },
        { id: null, relationshipName: '', conceptsList: [], displayOrder: 2 },
        { id: null, relationshipName: '', conceptsList: [], displayOrder: 3 }
      ],
      newConcept: ['', '', ''],
      showConceptInput: [false, false, false],
      saving: [false, false, false],
      loading: false
    };
  },
  mounted() {
    this.loadConcepts();
  },
  methods: {
    async loadConcepts() {
      this.loading = true;
      try {
        const response = await axios.get('http://localhost:9090/custom-concepts/list');
        
        // Reset concepts to default empty state first
        this.concepts = [
          { id: null, relationshipName: '', conceptsList: [], displayOrder: 1 },
          { id: null, relationshipName: '', conceptsList: [], displayOrder: 2 },
          { id: null, relationshipName: '', conceptsList: [], displayOrder: 3 }
        ];
        
        if (response.data.code === '200' && response.data.data) {
          response.data.data.forEach(concept => {
            const index = concept.displayOrder - 1;
            if (index >= 0 && index < 3) {
              this.concepts[index] = {
                id: concept.id,
                relationshipName: concept.relationshipName,
                conceptsList: concept.concepts ? concept.concepts.split(';').filter(c => c.trim()) : [],
                displayOrder: concept.displayOrder
              };
            }
          });
        }
      } catch (error) {
        console.error('加载自定义概念失败:', error);
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    
    showInputForConcept(order) {
      const index = order - 1;
      if (this.concepts[index].conceptsList.length >= 5) {
        this.$message.warning('每个组合最多5个概念');
        return;
      }
      this.$set(this.showConceptInput, index, true);
      this.$nextTick(() => {
        if (this.$refs.conceptInput && this.$refs.conceptInput[index]) {
          this.$refs.conceptInput[index].focus();
        }
      });
    },
    
    addConcept(order) {
      const index = order - 1;
      const concept = this.newConcept[index].trim();
      
      if (concept) {
        if (this.concepts[index].conceptsList.length >= 5) {
          this.$message.warning('每个组合最多5个概念');
        } else if (this.concepts[index].conceptsList.includes(concept)) {
          this.$message.warning('概念已存在');
        } else {
          this.concepts[index].conceptsList.push(concept);
        }
      }
      
      this.$set(this.newConcept, index, '');
      this.$set(this.showConceptInput, index, false);
    },
    
    removeConcept(order, conceptIndex) {
      this.concepts[order - 1].conceptsList.splice(conceptIndex, 1);
    },
    
    canSave(order) {
      const concept = this.concepts[order - 1];
      return concept.relationshipName.trim() !== '' && concept.conceptsList.length > 0;
    },
    
    async saveConcept(order) {
      const index = order - 1;
      const concept = this.concepts[index];
      
      if (!this.canSave(order)) {
        this.$message.warning('请填写关系名称并至少添加一个概念');
        return;
      }
      
      this.$set(this.saving, index, true);
      
      try {
        const data = {
          relationshipName: concept.relationshipName.trim(),
          concepts: concept.conceptsList.join(';'),
          displayOrder: order
        };
        
        const response = await axios.post('http://localhost:9090/custom-concepts/save', data);
        
        if (response.data.code === '200') {
          this.$message.success('保存成功');
          // Reload to get the saved ID and ensure data is fresh
          await this.loadConcepts();
        } else {
          this.$message.error(response.data.msg || '保存失败');
        }
      } catch (error) {
        console.error('保存失败:', error);
        this.$message.error('保存失败：' + (error.response?.data?.msg || '服务器错误'));
      } finally {
        this.$set(this.saving, index, false);
      }
    },
    
    async deleteConcept(order) {
      this.$confirm('确定要删除这个概念组合吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await axios.delete(`http://localhost:9090/custom-concepts/${order}`);
          if (response.data.code === '200') {
            this.$message.success('删除成功');
            const index = order - 1;
            this.concepts[index] = {
              id: null,
              relationshipName: '',
              conceptsList: [],
              displayOrder: order
            };
          } else {
            this.$message.error(response.data.msg || '删除失败');
          }
        } catch (error) {
          console.error('删除失败:', error);
          this.$message.error('删除失败');
        }
      }).catch(() => {
        // User cancelled
      });
    }
  }
};
</script>

<style scoped>
.personalization-container {
  max-width: 1200px;
  margin: 30px auto;
  padding: 30px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h2 {
  color: #303133;
  margin-bottom: 10px;
}

.description {
  color: #909399;
  font-size: 14px;
}

.concept-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.concepts-input {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.concept-tag {
  font-size: 14px;
  padding: 8px 12px;
}

.input-new-concept {
  width: 200px;
}

.button-new-concept {
  border-style: dashed;
}

.save-tip {
  margin-left: 15px;
  color: #67C23A;
  font-size: 14px;
}

.info-section {
  margin-top: 40px;
}

.info-section h4 {
  margin-bottom: 10px;
  color: #409EFF;
}

.info-section ul {
  margin: 10px 0 0 20px;
  line-height: 1.8;
}

.info-section li {
  color: #606266;
  font-size: 14px;
}
</style>
