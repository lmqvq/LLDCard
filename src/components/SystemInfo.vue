<template>
  <div class="system-info-container">
    <header class="page-header">
      <div>
        <h2>系统信息</h2>
        <p class="subtitle">版本、维护仓库与上游来源</p>
      </div>
      <el-button type="primary" plain :loading="checking" @click="checkUpdate">
        检查更新
      </el-button>
    </header>

    <div class="info-grid">
      <el-card class="info-card" shadow="never">
        <template #header>
          <span class="card-title">基本信息</span>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="系统名称">LLDCard 卡密管理系统</el-descriptions-item>
          <el-descriptions-item label="当前版本">{{ currentVersion }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">2026-07-31</el-descriptions-item>
          <el-descriptions-item label="技术栈">Vue 3 + Spring Boot 3</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="info-card" shadow="never">
        <template #header>
          <span class="card-title">项目仓库</span>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="维护者">lmqvq</el-descriptions-item>
          <el-descriptions-item label="LLDCard">
            <a href="https://github.com/lmqvq/LLDCard" target="_blank" rel="noreferrer" class="link">
              github.com/lmqvq/LLDCard
            </a>
          </el-descriptions-item>
          <el-descriptions-item label="上游项目">
            <a
              href="https://github.com/xiaoxiaoguai-yyds/xxgkami-pro"
              target="_blank"
              rel="noreferrer"
              class="link"
            >
              xiaoxiaoguai-yyds/xxgkami-pro
            </a>
          </el-descriptions-item>
          <el-descriptions-item label="上游作者">小小怪 / xiaoxiaoguai-yyds</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="info-card provenance-card" shadow="never">
        <template #header>
          <span class="card-title">来源与许可状态</span>
        </template>
        <p>
          LLDCard 基于 xxgkami-pro 二次开发，并保留上游来源说明。
        </p>
        <el-alert
          title="上游仓库未随附明确的 LICENSE 文件，历史文档中的许可证表述互相冲突。使用或再分发前请向上游确认授权条件。"
          type="warning"
          :closable="false"
          show-icon
        />
      </el-card>
    </div>

    <section class="release-section">
      <div class="release-heading">
        <div>
          <span class="release-label">LLDCard release</span>
          <h3>v1.0.0 二开版本</h3>
        </div>
        <el-tag type="success" effect="plain">Docker Ready</el-tag>
      </div>
      <ul>
        <li>统一项目品牌、前后端包名与仓库链接。</li>
        <li>新增前端、后端、MySQL 和 Redis 的容器化部署。</li>
        <li>移除旧作者个人联系方式、赞助入口、旧域名和旧更新脚本。</li>
        <li>敏感文件与真实业务数据默认排除，不进入公开仓库或镜像。</li>
      </ul>
    </section>

    <el-dialog v-model="showUpdateDialog" title="发现新版本" width="500px">
      <div v-if="updateInfo" class="update-dialog">
        <div class="new-version">最新版本：v{{ updateInfo.version }}</div>
        <div class="update-date">发布时间：{{ updateInfo.buildDate }}</div>
        <ul>
          <li v-for="(item, index) in updateInfo.changelog" :key="index">{{ item }}</li>
        </ul>
      </div>
      <template #footer>
        <el-button @click="showUpdateDialog = false">关闭</el-button>
        <el-button type="primary" @click="goToRepo">前往仓库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const currentVersion = 'v1.0.0'
const checking = ref(false)
const showUpdateDialog = ref(false)
const updateInfo = ref(null)

const checkUpdate = async () => {
  checking.value = true
  try {
    const response = await fetch('/api/monitor/check-update')
    if (!response.ok) {
      throw new Error('Update check failed')
    }

    const data = await response.json()
    if (data.version !== currentVersion.slice(1)) {
      updateInfo.value = data
      showUpdateDialog.value = true
    } else {
      ElMessage.success('当前已是最新版本')
    }
  } catch (error) {
    console.error('Update check failed:', error)
    ElMessage.error('检查更新失败，请稍后重试')
  } finally {
    checking.value = false
  }
}

const goToRepo = () => {
  const repoUrl = updateInfo.value?.repoUrl || 'https://github.com/lmqvq/LLDCard'
  window.open(repoUrl, '_blank', 'noopener,noreferrer')
  showUpdateDialog.value = false
}
</script>

<style scoped>
.system-info-container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #111827;
  font-size: 24px;
}

.subtitle {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.info-card {
  border-radius: 6px;
}

.provenance-card {
  grid-column: 1 / -1;
}

.card-title {
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}

.provenance-card p {
  margin: 0 0 14px;
  color: #374151;
  line-height: 1.7;
}

.link {
  color: #2563eb;
  overflow-wrap: anywhere;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.release-section {
  margin-top: 24px;
  padding: 20px 0;
  border-top: 1px solid #e5e7eb;
}

.release-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.release-heading h3 {
  margin: 4px 0 0;
  color: #111827;
  font-size: 18px;
}

.release-label {
  color: #6b7280;
  font-size: 12px;
  text-transform: uppercase;
}

.release-section ul,
.update-dialog ul {
  margin: 16px 0 0;
  padding-left: 20px;
  color: #4b5563;
  line-height: 1.8;
}

.new-version {
  color: #111827;
  font-size: 18px;
  font-weight: 600;
}

.update-date {
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
}

@media (max-width: 760px) {
  .system-info-container {
    padding: 12px;
  }

  .page-header {
    align-items: flex-start;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .provenance-card {
    grid-column: auto;
  }
}
</style>