<template>
  <div>
    <!--Header-->
    <div class="front-header">
      <div class="front-header-left">
        <img src="@/assets/imgs/logo.png" alt="">
        <div class="title">Literature Management & Analysis System</div>
      </div>
      <div class="front-header-center">
        <div class="front-header-nav">
          <el-menu :default-active="$route.path" mode="horizontal" router>
            <el-menu-item index="/front/graph">Knowledge Graph</el-menu-item>
            <el-menu-item index="/front/graph-personalization">Graph Personalization</el-menu-item>
            <el-menu-item index="/front/home">Search</el-menu-item>
            <el-menu-item index="/front/upload">Paper Upload</el-menu-item>
          </el-menu>
        </div>
      </div>
      <div class="front-header-right">
        <el-button icon="el-icon-setting" circle @click="$router.push('/front/settings')"></el-button>
      </div>
    </div>
    <!--Main Content-->
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