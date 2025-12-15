<template>
  <!-- 路由出口：所有页面组件会渲染到这里 -->
  <router-view />
</template>

<script setup lang="ts">
// 根组件无需额外逻辑，主要用于承载路由视图
</script>

<style>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Helvetica Neue", Arial, sans-serif;
  background-color: #f5f7fa;
  color: #333;
}

/* 解决 Element Plus 表格边框显示问题 */
.el-table th, .el-table td {
  border-right: 1px solid #ebeef5;
}
.el-table tr {
  border-bottom: 1px solid #ebeef5;
}
</style>