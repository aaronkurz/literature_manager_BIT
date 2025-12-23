<template>
  <div class="container">
    <el-container>
      <el-main>
        <div id="dashboard">
          <div id="viz-container">
            <div id="viz"></div>
          </div>
        </div>
      </el-main>
      <el-aside width="350px">
        <div id="controls">
          <!-- Custom Query Section -->
          <el-card class="control-card legend-box" shadow="hover">
            <h3 slot="header">自定义查询</h3>
            <el-input
                type="textarea"
                :rows="3"
                v-model="cypherQuery"
                placeholder="输入Cypher查询语句，例如：MATCH (p:论文)-[:作者]->(a) RETURN p,a LIMIT 100"
            ></el-input>
            <div class="button-group">
              <el-button type="primary" @click="handleReload">执行查询</el-button>
              <el-button type="success" @click="viz?.stabilize()">稳定布局</el-button>
              <el-button type="warning" @click="handleRebuildGraph" :loading="rebuilding">重建图谱</el-button>
            </div>
            <div class="legend-section">
              <h4>节点类型：</h4>
              <el-row :gutter="10" class="legend-grid">
                <el-col :span="12" v-for="item in nodeTypes" :key="item.label">
                  <div class="legend-item">
                    <div :class="['node-sample', item.class]"></div>
                    {{ item.label }}
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>

          <!-- Relationship Query Section -->
          <el-card class="control-card" shadow="hover">
            <h3 slot="header">关系查询</h3>
            <el-row :gutter="15" class="button-grid">
              <el-col :span="8" v-for="query in relationQueries" :key="query.label">
                <el-button
                    type="info"
                    @click="runQuery(query.cypher)"
                    class="query-button"
                >{{ query.label }}</el-button>
              </el-col>
            </el-row>
          </el-card>

          <!-- Node Query Section -->
          <el-card class="control-card" shadow="hover">
            <h3 slot="header">节点查询</h3>
            <el-row :gutter="15" class="button-grid">
              <el-col :span="8" v-for="query in nodeQueries" :key="query.label">
                <el-button
                    type="primary"
                    @click="runQuery(query.cypher)"
                    class="query-button"
                >{{ query.label }}</el-button>
              </el-col>
            </el-row>
          </el-card>

          <!-- Custom Concepts Query Section -->
          <el-card v-if="customConceptQueries.length > 0" class="control-card" shadow="hover">
            <h3 slot="header">自定义概念查询</h3>
            <el-row :gutter="15" class="button-grid">
              <el-col :span="8" v-for="query in customConceptQueries" :key="query.label">
                <el-button
                    type="success"
                    @click="runQuery(query.cypher)"
                    class="query-button"
                >{{ query.label }}</el-button>
              </el-col>
            </el-row>
          </el-card>

          <!-- Neo4j Browser Link -->
          <el-card class="control-card" shadow="hover">
            <h3 slot="header">Neo4j Browser</h3>
            <p>The Neo4j Browser cannot be embedded. Open it in a new tab:</p>
            <p><strong>URL:</strong> <a href="http://localhost:7474/browser/" target="_blank" rel="noopener">http://localhost:7474/browser/</a></p>
            <p><strong>Credentials (prototype):</strong> username: <code>neo4j</code>, password: <code>12345678</code></p>
            <div style="text-align:right;">
              <el-button type="primary" @click="openNeo4jBrowser">Open</el-button>
            </div>
          </el-card>

        </div>
      </el-aside>
    </el-container>
  </div>
</template>

<script>
import NeoVis from 'neovis.js';
import axios from 'axios';

export default {
  name: 'KnowledgeGraph',
  data() {
    return {
      viz: null,
      cypherQuery: '',
      rebuilding: false, // 控制重建按钮的加载状态
      config: {
        containerId: "viz",
        neo4j: {
          serverUrl: "bolt://localhost:7687",
          serverUser: "neo4j",
          serverPassword: "12345678"
        },
        labels: {
          "论文": { label: "title", color: "#FF6B6B", size: 40, font: { size: 18 } },
          "作者": { label: "name", color: "#ff00a6", size: 30 },
          "文献来源": { label: "name", color: "#45B7D1", size: 25 },
          "单位": { label: "name", color: "#96CEB4", size: 20 },
          "关键词": { label: "name", color: "#FFEEAD", size: 15 },
          "基金": { label: "name", color: "#FFA07A", size: 20 },
          "分类号": { label: "code", color: "#9B59B6", size: 15 },
          "来源库": { label: "name", color: "#D8BFD8", size: 20 },
          "自定义概念": { label: "name", color: "#67C23A", size: 25 }
        },
        relationships: {
          "作者": { color: "#FF4500", thickness: "weight", caption: true },
          "所属": { color: "#20B2AA", thickness: 2, caption: true },
          "隶属于": { color: "#2ECC71", curvature: 0.5, caption: true },
          "关键词": { color: "#FFD700", caption: true },
          "资助": { color: "#32CD32", curvature: 0.3, caption: true },
          "分类": { color: "#8A2BE2", thickness: 1.5, caption: true },
          "来源库": { color: "#DDA0DD", dash: [5, 5], caption: true },
          "自定义概念关系": { color: "#67C23A", thickness: 2.5, caption: true }
        },
        initialCypher: "MATCH p=()-->() RETURN p LIMIT 50"
      },
      nodeTypes: [
        { label: "论文", class: "paper" },
        { label: "作者", class: "author" },
        { label: "文献来源", class: "university" },
        { label: "单位", class: "institution" },
        { label: "关键词", class: "keyword" },
        { label: "基金", class: "fund" },
        { label: "分类号", class: "category" },
        { label: "来源库", class: "source" },
        { label: "自定义概念", class: "custom-concept" }
      ],
      relationQueries: [
        { label: "全部关系", cypher: "MATCH p=()-->() RETURN p LIMIT 50" },
        { label: "论文-作者", cypher: "MATCH p=()-[r:`作者`]->() RETURN p LIMIT 50" },
        { label: "论文-关键词", cypher: "MATCH p=()-[r:`关键词`]->() RETURN p LIMIT 50" },
        { label: "论文-分类", cypher: "MATCH p=()-[r:`分类`]->() RETURN p LIMIT 50" },
        { label: "论文-概念", cypher: "MATCH p=(论文:`论文`)-[r:`自定义概念关系`]->(c:`自定义概念`) RETURN p, r, c LIMIT 50" },
        { label: "作者-文献来源", cypher: "MATCH p=()-[r:`所属`]->() RETURN p LIMIT 50" },
        { label: "论文-来源库", cypher: "MATCH p=()-[r:`来源库`]->() RETURN p LIMIT 50" },
        { label: "基金-论文", cypher: "MATCH p=()-[r:`资助`]->() RETURN p LIMIT 50" },
        { label: "文献来源-单位", cypher: "MATCH p=()-[r:`隶属于`]->() RETURN p LIMIT 50" },
        { label: "查找某个论文", cypher: "MATCH (n:`论文` {title: '基于电子病历知识图谱的隐含疾病信息挖掘与应用研究'})-[r]-(m) RETURN n, r, m" }
      ],
      nodeQueries: [
        { label: "全部节点", cypher: "MATCH (n) RETURN n LIMIT 25" },
        { label: "作者", cypher: "MATCH (n:`作者`) RETURN n LIMIT 25" },
        { label: "关键词", cypher: "MATCH (n:`关键词`) RETURN n LIMIT 25" },
        { label: "分类号", cypher: "MATCH (n:`分类号`) RETURN n LIMIT 25" },
        { label: "基金", cypher: "MATCH (n:`基金`) RETURN n LIMIT 25" },
        { label: "文献来源", cypher: "MATCH (n:`文献来源`) RETURN n LIMIT 25" },
        { label: "单位", cypher: "MATCH (n:`单位`) RETURN n LIMIT 25" },
        { label: "来源库", cypher: "MATCH (n:`来源库`) RETURN n LIMIT 25" },
        { label: "论文", cypher: "MATCH (n:`论文`) RETURN n LIMIT 25" },
        { label: "自定义概念", cypher: "MATCH (n:`自定义概念`) RETURN n LIMIT 25" }
      ],
      customConceptQueries: []
    };
  },
  mounted() {
    this.initializeViz();
    this.loadCustomConcepts();
  },
  methods: {
    initializeViz() {
      this.viz = new NeoVis(this.config);
      this.viz.render();
    },
    async loadCustomConcepts() {
      try {
        const response = await axios.get('http://localhost:9090/custom-concepts/list');
        if (response.data.code === '200' && response.data.data) {
          const concepts = response.data.data;
          this.customConceptQueries = [];
          
          // Generate queries for each custom concept
          concepts.forEach(concept => {
            // Query for all papers with this relationship type
            this.customConceptQueries.push({
              label: `全部${concept.relationshipName}`,
              cypher: `MATCH p=(论文:\`论文\`)-[:\`自定义概念关系\`]->(c:\`自定义概念\`) WHERE c.relationship = '${concept.relationshipName}' RETURN p LIMIT 50`
            });
            
            // Query for each specific concept value
            const conceptsList = concept.concepts.split(';').filter(c => c.trim());
            conceptsList.forEach(conceptValue => {
              this.customConceptQueries.push({
                label: `${conceptValue}`,
                cypher: `MATCH p=(论文:\`论文\`)-[:\`自定义概念关系\`]->(c:\`自定义概念\` {name: '${conceptValue}', relationship: '${concept.relationshipName}'}) RETURN p LIMIT 50`
              });
            });
          });
        }
      } catch (error) {
        console.error('加载自定义概念失败:', error);
      }
    },
    runQuery(cypher) {
      this.cypherQuery = cypher;
      this.viz?.renderWithCypher(cypher);
    },
    handleReload() {
      if (this.cypherQuery.length > 3) {
        this.viz?.renderWithCypher(this.cypherQuery);
      } else {
        this.viz?.reload();
      }
    },
    handleRebuildGraph() {
      this.$confirm('确定要重建图谱吗？此操作可能需要一些时间。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.rebuilding = true; // 显示按钮上的加载动画
        const loadingMessage = this.$message({
          message: '正在重建图谱，请稍候...',
          type: 'info',
          showClose: false, // 不显示关闭按钮
          duration: 0, // 持续显示，直到手动关闭
          iconClass: 'el-icon-loading'
        });

        axios.post('http://localhost:9090/article/rebuild')
            .then(response => {
              this.rebuilding = false;
              loadingMessage.close(); // 关闭加载提示
              if (response.data.code === '200') {
                this.$message.success('图谱重建成功！');
                this.viz?.reload();
              } else {
                this.$message.error('图谱重建失败：' + response.data.msg);
              }
            })
            .catch(error => {
              this.rebuilding = false;
              loadingMessage.close(); // 关闭加载提示
              console.error('重建图谱失败：', error);
              this.$message.error('重建图谱时发生错误，请稍后重试。');
            });
      }).catch(() => {
        this.$message.info('已取消重建');
      });
    },
    openNeo4jBrowser() {
      window.open('http://localhost:7474/browser/', '_blank', 'noopener');
    }
  }
};
</script>

<style scoped>
.container {
  padding: 20px;
  background: #f5f6fa;
  min-height: 100vh;
}

#dashboard {
  display: flex;
  flex-direction: row;
  gap: 25px;
}

#viz-container {
  flex: 1;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

#viz {
  width: 100%;
  height: 750px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: #fafafa;
}

#controls {
  width: 350px;
}

.control-card {
  margin-bottom: 20px;
}

.button-group {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.legend-section {
  margin-top: 20px;
}

.legend-grid {
  margin-top: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #34495e;
  margin-bottom: 10px;
}

.node-sample {
  display: inline-block;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  margin-right: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.paper {
  background: #97C2FC;
}

.author {
  background: #7BE141;
}

.university {
  background: #EB7DF4;
}

.institution {
  background: #96CEB4;
}

.keyword {
  background: #AD85E4;
}

.fund {
  background: #FFA807;
}

.category {
  background: #FFFF00;
}

.source {
  background: #FB7E81;
}

.custom-concept {
  background: #67C23A;
}

.button-grid {
  margin-top: 15px;
}

.query-button {
  width: 100%;
  padding: 8px 0;
  font-size: 12px;
  border-radius: 4px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 10px;
}

.el-button--primary {
  background-color: #1890FF;
  border-color: #1890FF;
}

.el-button--primary:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}
</style>