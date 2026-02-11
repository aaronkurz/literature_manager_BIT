<template>
  <div class="personalization-container">
    <div class="header">
      <h2>Graph Personalization Settings</h2>
      <p class="description">Customize paper concept relationships, up to 3 groups, each with up to 5 concepts</p>
    </div>

    <el-card v-for="order in [1, 2, 3]" :key="order" class="concept-card" shadow="hover">
      <div slot="header" class="card-header">
        <span>Concept Group {{ order }}</span>
        <el-button 
          v-if="concepts[order - 1] && concepts[order - 1].relationshipName"
          type="danger" 
          size="small" 
          @click="deleteConcept(order)"
          icon="el-icon-delete"
        >Delete</el-button>
      </div>

      <el-form :model="concepts[order - 1]" label-width="120px">
        <el-form-item label="Relationship Name">
          <el-input 
            v-model="concepts[order - 1].relationshipName" 
            placeholder="例如: method, dataset, evaluation_metric"
            :maxlength="50"
          ></el-input>
          <div class="tip">A meaningful relationship name describing the link between paper and concepts</div>
        </el-form-item>

        <el-form-item label="Concept List">
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
              placeholder="Enter concept name"
              :maxlength="100"
            ></el-input>
            
            <el-button
              v-else
              size="small"
              @click="showInputForConcept(order)"
              :disabled="concepts[order - 1].conceptsList.length >= 5"
              class="button-new-concept"
            >+ Add Concept</el-button>
          </div>
          <div class="tip">
            Up to 5 concepts. Example: RCT, Retrospective_Cohort, Prospective_Cohort, Mendelian_Randomization_Study
          </div>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            @click="saveConcept(order)"
            :disabled="!canSave(order)"
            :loading="saving[order - 1]"
          >Save Concept Group</el-button>
          <span v-if="concepts[order - 1].id" class="save-tip">
            <i class="el-icon-success"></i> Saved
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="info-section">
      <el-alert type="info" :closable="false">
        <h4>Instructions</h4>
        <ul>
          <li><strong>Relationship Name</strong>: Describes the relationship type between papers and concepts (e.g., method, dataset, evaluation_metric)</li>
          <li><strong>Concept List</strong>: Specific concept values that may apply to papers (e.g., RCT, Cohort Study)</li>
          <li>When uploading papers, AI will automatically identify if these concepts are used</li>
          <li>Custom concepts can be used to filter and query in the knowledge graph</li>
          <li>Concept names should use English or abbreviations, avoid special characters</li>
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
        console.error('Failed to load custom concepts:', error);
        this.$message.error('Loading failed');
      } finally {
        this.loading = false;
      }
    },
    
    showInputForConcept(order) {
      const index = order - 1;
      if (this.concepts[index].conceptsList.length >= 5) {
        this.$message.warning('Maximum 5 concepts per group');
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
          this.$message.warning('Maximum 5 concepts per group');
        } else if (this.concepts[index].conceptsList.includes(concept)) {
          this.$message.warning('Concept already exists');
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
        this.$message.warning('Please enter a relationship name and add at least one concept');
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
          this.$message.success('Saved successfully');
          // Reload to get the saved ID and ensure data is fresh
          await this.loadConcepts();
        } else {
          this.$message.error(response.data.msg || 'Save failed');
        }
      } catch (error) {
        console.error('Save failed:', error);
        this.$message.error('Save failed: ' + (error.response?.data?.msg || 'Server error'));
      } finally {
        this.$set(this.saving, index, false);
      }
    },
    
    async deleteConcept(order) {
      this.$confirm('Are you sure you want to delete this concept group?', 'Confirm', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await axios.delete(`http://localhost:9090/custom-concepts/${order}`);
          if (response.data.code === '200') {
            this.$message.success('Deleted successfully');
            const index = order - 1;
            this.concepts[index] = {
              id: null,
              relationshipName: '',
              conceptsList: [],
              displayOrder: order
            };
          } else {
            this.$message.error(response.data.msg || 'Delete failed');
          }
        } catch (error) {
          console.error('Delete failed:', error);
          this.$message.error('Delete failed');
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
