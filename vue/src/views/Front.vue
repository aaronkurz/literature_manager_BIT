<template>
  <div>
    <!--头部-->
    <div class="front-header">
      <div class="front-header-left">
        <img src="@/assets/imgs/logo.png" alt="">
        <div class="title">文献管理与分析系统</div>
      </div>
      <div class="front-header-center">
        <div class="front-header-nav">
          <el-menu :default-active="$route.path" mode="horizontal" router>
            <el-menu-item index="/front/graph">知识图谱</el-menu-item>
            <el-menu-item index="/front/graph-personalization">图谱个性化</el-menu-item>
            <el-menu-item index="/front/home">搜索</el-menu-item>
            <el-menu-item index="/front/upload">论文上传</el-menu-item>
          </el-menu>
        </div>
      </div>
      <div class="front-header-right">
        <el-button icon="el-icon-setting" circle @click="$router.push('/front/settings')"></el-button>
      </div>
    </div>
    <!--主体-->
    <div class="main-body">
      <router-view ref="child" />
    </div>
  </div>

</template>

<script>

export default {
  name: "FrontLayout",

  data () {
    return {
    }
  },

  mounted() {
  },
  methods: {
    openExternalLink() {
      location.href = 'http://localhost:7474/browser/'
    },
  }

}
</script>

<style scoped>
  @import "@/assets/css/front.css";
</style>