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
            <h3 slot="header">Custom Query</h3>
            <el-input
                type="textarea"
                :rows="3"
                v-model="cypherQuery"
                placeholder="Enter Cypher query, e.g.: MATCH (p:Paper)-[:AUTHORED_BY]->(a) RETURN p,a LIMIT 100"
            ></el-input>
            <div class="button-group">
              <el-button type="primary" @click="handleReload">Run Query</el-button>
              <el-button type="success" @click="viz?.stabilize()">Stabilize Layout</el-button>
              <el-button type="warning" @click="handleRebuildGraph" :loading="rebuilding">Rebuild Graph</el-button>
            </div>
            <div class="legend-section">
              <h4>Node Types:</h4>
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
            <h3 slot="header">Relationship Query</h3>
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
            <h3 slot="header">Node Query</h3>
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
            <h3 slot="header">Custom Concept Query</h3>
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
      rebuilding: false, // Rebuild button loading state
      config: {
        containerId: "viz",
        neo4j: {
          serverUrl: "bolt://localhost:7687",
          serverUser: "neo4j",
          serverPassword: "12345678"
        },
        labels: {
          "Paper": { label: "title", color: "#FF6B6B", size: 40, font: { size: 18 } },
          "Author": { label: "name", color: "#ff00a6", size: 30 },
          "Source": { label: "name", color: "#45B7D1", size: 25 },
          "Institution": { label: "name", color: "#96CEB4", size: 20 },
          "Keyword": { label: "name", color: "#FFEEAD", size: 15 },
          "Fund": { label: "name", color: "#FFA07A", size: 20 },
          "Category": { label: "code", color: "#9B59B6", size: 15 },
          "SourceDB": { label: "name", color: "#D8BFD8", size: 20 },
          "CustomConcept": { label: "name", color: "#67C23A", size: 25 }
        },
        relationships: {
          "AUTHORED_BY": { color: "#FF4500", thickness: "weight", caption: true },
          "BELONGS_TO": { color: "#20B2AA", thickness: 2, caption: true },
          "AFFILIATED_WITH": { color: "#2ECC71", curvature: 0.5, caption: true },
          "HAS_KEYWORD": { color: "#FFD700", caption: true },
          "FUNDED_BY": { color: "#32CD32", curvature: 0.3, caption: true },
          "CLASSIFIED_AS": { color: "#8A2BE2", thickness: 1.5, caption: true },
          "FROM_SOURCE": { color: "#DDA0DD", dash: [5, 5], caption: true },
          "HAS_CUSTOM_CONCEPT": { color: "#67C23A", thickness: 2.5, caption: true }
        },
        initialCypher: "MATCH p=()-->() RETURN p LIMIT 50"
      },
      nodeTypes: [
        { label: "Paper", class: "paper" },
        { label: "Author", class: "author" },
        { label: "Source", class: "university" },
        { label: "Institution", class: "institution" },
        { label: "Keyword", class: "keyword" },
        { label: "Fund", class: "fund" },
        { label: "Category", class: "category" },
        { label: "SourceDB", class: "source" },
        { label: "CustomConcept", class: "custom-concept" }
      ],
      relationQueries: [
        { label: "All Relations", cypher: "MATCH p=()-->() RETURN p LIMIT 50" },
        { label: "Paper-Author", cypher: "MATCH p=()-[r:`AUTHORED_BY`]->() RETURN p LIMIT 50" },
        { label: "Paper-Keyword", cypher: "MATCH p=()-[r:`HAS_KEYWORD`]->() RETURN p LIMIT 50" },
        { label: "Paper-Category", cypher: "MATCH p=()-[r:`CLASSIFIED_AS`]->() RETURN p LIMIT 50" },
        { label: "Paper-Concept", cypher: "MATCH p=(paper:`Paper`)-[r:`HAS_CUSTOM_CONCEPT`]->(c:`CustomConcept`) RETURN p, r, c LIMIT 50" },
        { label: "Author-Source", cypher: "MATCH p=()-[r:`BELONGS_TO`]->() RETURN p LIMIT 50" },
        { label: "Paper-SourceDB", cypher: "MATCH p=()-[r:`FROM_SOURCE`]->() RETURN p LIMIT 50" },
        { label: "Fund-Paper", cypher: "MATCH p=()-[r:`FUNDED_BY`]->() RETURN p LIMIT 50" },
        { label: "Source-Institution", cypher: "MATCH p=()-[r:`AFFILIATED_WITH`]->() RETURN p LIMIT 50" },
        { label: "Find Specific Paper", cypher: "MATCH (n:`Paper` {title: 'Example Paper Title'})-[r]-(m) RETURN n, r, m" }
      ],
      nodeQueries: [
        { label: "All Nodes", cypher: "MATCH (n) RETURN n LIMIT 25" },
        { label: "Author", cypher: "MATCH (n:`Author`) RETURN n LIMIT 25" },
        { label: "Keyword", cypher: "MATCH (n:`Keyword`) RETURN n LIMIT 25" },
        { label: "Category", cypher: "MATCH (n:`Category`) RETURN n LIMIT 25" },
        { label: "Fund", cypher: "MATCH (n:`Fund`) RETURN n LIMIT 25" },
        { label: "Source", cypher: "MATCH (n:`Source`) RETURN n LIMIT 25" },
        { label: "Institution", cypher: "MATCH (n:`Institution`) RETURN n LIMIT 25" },
        { label: "SourceDB", cypher: "MATCH (n:`SourceDB`) RETURN n LIMIT 25" },
        { label: "Paper", cypher: "MATCH (n:`Paper`) RETURN n LIMIT 25" },
        { label: "CustomConcept", cypher: "MATCH (n:`CustomConcept`) RETURN n LIMIT 25" }
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
              label: `All ${concept.relationshipName}`,
              cypher: `MATCH p=(paper:\`Paper\`)-[:\`HAS_CUSTOM_CONCEPT\`]->(c:\`CustomConcept\`) WHERE c.relationship = '${concept.relationshipName}' RETURN p LIMIT 50`
            });
            
            // Query for each specific concept value
            const conceptsList = concept.concepts.split(';').filter(c => c.trim());
            conceptsList.forEach(conceptValue => {
              this.customConceptQueries.push({
                label: `${conceptValue}`,
                cypher: `MATCH p=(paper:\`Paper\`)-[:\`HAS_CUSTOM_CONCEPT\`]->(c:\`CustomConcept\` {name: '${conceptValue}', relationship: '${concept.relationshipName}'}) RETURN p LIMIT 50`
              });
            });
          });
        }
      } catch (error) {
        console.error('Failed to load custom concepts:', error);
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
      this.$confirm('Are you sure you want to rebuild the graph? This may take some time.', 'Warning', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        this.rebuilding = true; // Show loading animation on button
        const loadingMessage = this.$message({
          message: 'Rebuilding graph, please wait...',
          type: 'info',
          showClose: false, // Hide close button
          duration: 0, // Show persistently until manually closed
          iconClass: 'el-icon-loading'
        });

        axios.post('http://localhost:9090/article/rebuild')
            .then(response => {
              this.rebuilding = false;
              loadingMessage.close(); // Close loading message
              if (response.data.code === '200') {
                this.$message.success('Graph rebuilt successfully!');
                this.viz?.reload();
              } else {
                this.$message.error('Graph rebuild failed: ' + response.data.msg);
              }
            })
            .catch(error => {
              this.rebuilding = false;
              loadingMessage.close(); // Close loading message
              console.error('Graph rebuild failed: ', error);
              this.$message.error('An error occurred while rebuilding the graph. Please try again later.');
            });
      }).catch(() => {
        this.$message.info('Rebuild cancelled');
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