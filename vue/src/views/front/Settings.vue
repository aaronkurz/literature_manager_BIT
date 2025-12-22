<template>
  <div style="padding: 20px">
    <div style="margin-bottom: 20px">
      <el-page-header @back="$router.push('/front/home')" content="设置">
      </el-page-header>
    </div>

    <el-card>
      <div slot="header">
        <span>系统设置</span>
      </div>
      
      <el-form label-width="120px">
        <el-form-item label="系统名称">
          <el-input value="文献管理与分析系统" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="版本">
          <el-input value="Local Research Tool v1.0" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="AI 模型">
          <el-input value="Ollama - Ministral 3B" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="数据库">
          <el-input value="MySQL + Neo4j" disabled></el-input>
        </el-form-item>

        <el-divider></el-divider>

        <el-form-item label="关于">
          <p>本地研究文献管理原型工具</p>
          <p>功能：论文上传、AI 摘要生成、知识图谱可视化</p>
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
