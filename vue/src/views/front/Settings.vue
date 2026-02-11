<template>
  <div style="padding: 20px">
    <div style="margin-bottom: 20px">
      <el-page-header @back="$router.push('/front/home')" content="Settings">
      </el-page-header>
    </div>

    <el-card>
      <div slot="header">
        <span>System Settings</span>
      </div>
      
      <el-form label-width="120px">
        <el-form-item label="System Name">
          <el-input value="Literature Management & Analysis System" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="Version">
          <el-input value="Local Research Tool v1.0" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="AI Model">
          <el-input value="Ollama - Ministral-3 (3B, 256K context)" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="Database">
          <el-input value="MySQL + Neo4j" disabled></el-input>
        </el-form-item>

        <el-divider></el-divider>

        <el-form-item label="About">
          <p>Local research literature management prototype tool</p>
          <p>Features: Paper upload, AI summary generation, knowledge graph visualization</p>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Settings',
  data() {
    return {}
  }
}
</script>

<style scoped>
</style>
