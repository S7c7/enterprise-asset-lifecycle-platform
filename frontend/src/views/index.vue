<template>
  <div class="asset-home">
    <section class="hero-section">
      <div class="hero-copy">
        <div class="eyebrow"><span class="eyebrow-dot"></span>企业级资产数字化底座</div>
        <h1>让每一项资产，从申请到退出<br /><span>全程在线、清晰可控</span></h1>
        <p class="hero-description">
          企业资产全生命周期管理平台面向集团型企业和大型组织，将资产申请、审批、采购、入库、领用、调拨、归还、维修与报废纳入统一流程，让资产状态可查询、责任可追溯、过程可审计。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="goTo('/asset/info')">
            <el-icon><Box /></el-icon>
            查看资产台账
          </el-button>
          <el-button size="large" round @click="goTo('/asset/application')">
            <el-icon><Promotion /></el-icon>
            发起资产申请
          </el-button>
        </div>
        <div class="hero-assurances">
          <span
            ><el-icon><CircleCheckFilled /></el-icon>统一资产编码</span
          >
          <span
            ><el-icon><CircleCheckFilled /></el-icon>流程全程留痕</span
          >
          <span
            ><el-icon><CircleCheckFilled /></el-icon>组织权限隔离</span
          >
        </div>
      </div>

      <div class="hero-visual" aria-hidden="true">
        <div class="visual-glow"></div>
        <div class="orbit orbit-outer"></div>
        <div class="orbit orbit-inner"></div>
        <div class="core-card">
          <div class="core-icon">
            <el-icon><Box /></el-icon>
          </div>
          <strong>资产全生命周期</strong>
          <span>One asset · One record</span>
        </div>
        <div class="float-card card-ledger">
          <el-icon><Tickets /></el-icon>
          <div><small>统一台账</small><b>一物一码</b></div>
        </div>
        <div class="float-card card-flow">
          <el-icon><Connection /></el-icon>
          <div><small>流程引擎</small><b>审批可追踪</b></div>
        </div>
        <div class="float-card card-security">
          <el-icon><Lock /></el-icon>
          <div><small>权限治理</small><b>数据有边界</b></div>
        </div>
      </div>
    </section>

    <section class="value-grid" aria-label="平台核心价值">
      <article v-for="item in valueCards" :key="item.label" class="value-card">
        <div class="value-number">{{ item.value }}</div>
        <div>
          <h3>{{ item.label }}</h3>
          <p>{{ item.description }}</p>
        </div>
      </article>
    </section>

    <section class="content-panel lifecycle-panel">
      <div class="section-heading">
        <div>
          <span class="section-kicker">ASSET LIFECYCLE</span>
          <h2>贯穿资产全生命周期</h2>
          <p>以统一台账为核心，让每一次业务动作都沉淀为可追溯的资产履历。</p>
        </div>
        <div class="legend">
          <span><i class="legend-dot ready"></i>已建设</span>
          <span><i class="legend-dot planned"></i>持续建设</span>
        </div>
      </div>

      <div class="lifecycle-track">
        <template v-for="(step, index) in lifecycleSteps" :key="step.name">
          <div class="lifecycle-step" :class="step.status">
            <div class="step-index">
              <el-icon v-if="step.status === 'ready'"><Check /></el-icon>
              <span v-else>{{ index + 1 }}</span>
            </div>
            <strong>{{ step.name }}</strong>
            <small>{{ step.caption }}</small>
          </div>
          <div v-if="index < lifecycleSteps.length - 1" class="step-line" :class="step.status"></div>
        </template>
      </div>
    </section>

    <section class="capability-section">
      <div class="section-heading compact">
        <div>
          <span class="section-kicker">CORE CAPABILITIES</span>
          <h2>围绕资产领域，而不只是后台框架</h2>
          <p>通用认证与权限能力由 RuoYi-Vue-Plus 提供，资产领域模型、流程与页面由本项目独立建设。</p>
        </div>
      </div>

      <div class="capability-grid">
        <article v-for="module in capabilityModules" :key="module.title" class="capability-card">
          <div class="capability-top">
            <div class="capability-icon" :class="module.tone">
              <el-icon><component :is="module.icon" /></el-icon>
            </div>
            <span class="status-tag" :class="module.status === '已上线' ? 'online' : 'roadmap'">{{ module.status }}</span>
          </div>
          <h3>{{ module.title }}</h3>
          <p>{{ module.description }}</p>
          <div class="capability-tags">
            <span v-for="tag in module.tags" :key="tag">{{ tag }}</span>
          </div>
        </article>
      </div>
    </section>

    <section class="architecture-panel">
      <div class="architecture-copy">
        <span class="section-kicker light">TECHNOLOGY FOUNDATION</span>
        <h2>成熟技术底座，支撑业务持续演进</h2>
        <p>前后端分离、模块化领域设计，兼顾开发效率、权限安全与后续扩展能力。</p>
        <div class="tech-tags">
          <span>Vue 3</span><span>TypeScript</span><span>Element Plus</span><span>Spring Boot</span><span>MyBatis-Plus</span><span>WarmFlow</span
          ><span>Sa-Token</span><span>MySQL</span><span>Redis</span>
        </div>
      </div>
      <div class="architecture-flow">
        <div class="arch-layer"><small>交互层</small><strong>资产工作台 · 业务页面</strong></div>
        <div class="arch-arrow">↓</div>
        <div class="arch-layer"><small>领域层</small><strong>台账 · 申请 · 审批 · 履历</strong></div>
        <div class="arch-arrow">↓</div>
        <div class="arch-layer"><small>基础层</small><strong>组织 · 权限 · 租户 · 数据</strong></div>
      </div>
    </section>

    <footer class="home-footer">
      <div>
        <strong>Enterprise Asset Lifecycle Management Platform</strong>
        <span>基于 RuoYi-Vue-Plus 与 plus-ui 进行合规二次开发</span>
      </div>
      <span class="footer-mark">资产有账 · 流程有痕 · 管理有据</span>
    </footer>
  </div>
</template>

<script setup name="Index" lang="ts">
import { Box, Check, CircleCheckFilled, Connection, DataAnalysis, DocumentChecked, Lock, Promotion, Tickets, Van } from '@element-plus/icons-vue';

const router = useRouter();

const valueCards = [
  { value: '2', label: '核心基础档案', description: '资产分类与资产台账' },
  { value: '5', label: '业务申请类型', description: '采购、领用、调拨、归还、报废' },
  { value: '1', label: '统一审批主链路', description: '流程状态与业务状态协同' },
  { value: '100%', label: '关键操作留痕', description: '责任、时间与状态可追溯' }
];

const lifecycleSteps = [
  { name: '申请', caption: '需求发起', status: 'ready' },
  { name: '审批', caption: '合规决策', status: 'ready' },
  { name: '采购', caption: '采购执行', status: 'planned' },
  { name: '入库', caption: '验收建账', status: 'planned' },
  { name: '领用', caption: '责任到人', status: 'planned' },
  { name: '调拨', caption: '组织流转', status: 'planned' },
  { name: '归还', caption: '状态回收', status: 'planned' },
  { name: '维修', caption: '维护履历', status: 'planned' },
  { name: '报废', caption: '规范退出', status: 'planned' }
];

const capabilityModules = [
  {
    title: '资产基础数据',
    description: '建立统一资产分类与资产台账，规范编码、状态、归属部门、保管人和存放位置。',
    tags: ['分类树', '资产台账', '状态管理'],
    status: '已上线',
    tone: 'blue',
    icon: Box
  },
  {
    title: '申请与审批协同',
    description: '用统一申请模型承载五类资产业务，通过 WarmFlow 串联提交、办理与结果回写。',
    tags: ['五类申请', '待办审批', '审批记录'],
    status: '已上线',
    tone: 'cyan',
    icon: DocumentChecked
  },
  {
    title: '采购与库存执行',
    description: '承接审批结果，逐步建设采购单、到货验收、入出库和库存盘点执行能力。',
    tags: ['采购单', '验收入库', '库存盘点'],
    status: '规划中',
    tone: 'orange',
    icon: Van
  },
  {
    title: '统计与治理',
    description: '以完整资产履历为基础，形成部门、分类、状态与价值维度的管理分析视图。',
    tags: ['数据权限', '资产趋势', '审计追踪'],
    status: '规划中',
    tone: 'purple',
    icon: DataAnalysis
  }
];

const goTo = (path: string) => router.push(path);
</script>

<style lang="scss" scoped>
.asset-home {
  min-height: calc(100vh - 84px);
  padding: 24px;
  color: #17243d;
  background:
    radial-gradient(circle at 9% 2%, rgba(49, 130, 246, 0.08), transparent 28%), linear-gradient(180deg, #f5f8fc 0%, #f8fafc 56%, #f3f6fa 100%);
}

.hero-section {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(380px, 0.82fr);
  min-height: 440px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 24px;
  background: linear-gradient(112deg, rgba(8, 33, 70, 0.98) 0%, rgba(12, 58, 103, 0.96) 52%, rgba(10, 92, 106, 0.9) 100%);
  box-shadow: 0 24px 70px rgba(27, 57, 95, 0.16);
}

.hero-section::before,
.hero-section::after {
  position: absolute;
  content: '';
  border-radius: 50%;
  pointer-events: none;
}

.hero-section::before {
  width: 360px;
  height: 360px;
  top: -220px;
  left: 36%;
  background: rgba(65, 184, 245, 0.12);
}

.hero-section::after {
  width: 280px;
  height: 280px;
  right: -120px;
  bottom: -160px;
  border: 54px solid rgba(48, 213, 200, 0.08);
}

.hero-copy {
  z-index: 1;
  align-self: center;
  padding: 62px 24px 58px 68px;
  color: #fff;
}

.eyebrow,
.section-kicker {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  color: #70ddda;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.eyebrow-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #56e3cc;
  box-shadow: 0 0 0 6px rgba(86, 227, 204, 0.12);
}

.hero-copy h1 {
  margin: 21px 0 20px;
  font-size: clamp(34px, 3.3vw, 52px);
  line-height: 1.22;
  letter-spacing: -0.04em;
}

.hero-copy h1 span {
  color: #7ce7dd;
}

.hero-description {
  max-width: 720px;
  margin: 0;
  color: rgba(232, 242, 255, 0.78);
  font-size: 15px;
  line-height: 1.9;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 31px;
}

.hero-actions :deep(.el-button--primary) {
  --el-button-bg-color: #2fb9b3;
  --el-button-border-color: #2fb9b3;
  --el-button-hover-bg-color: #43cbc4;
  --el-button-hover-border-color: #43cbc4;
  padding-inline: 25px;
  box-shadow: 0 10px 24px rgba(47, 185, 179, 0.25);
}

.hero-actions :deep(.el-button:not(.el-button--primary)) {
  color: #eaf4ff;
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.08);
}

.hero-assurances {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
  margin-top: 29px;
  color: rgba(226, 239, 253, 0.68);
  font-size: 12px;
}

.hero-assurances span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.hero-assurances .el-icon {
  color: #65d8cf;
}

.hero-visual {
  position: relative;
  z-index: 1;
  min-height: 430px;
}

.visual-glow {
  position: absolute;
  width: 300px;
  height: 300px;
  top: 70px;
  left: calc(50% - 150px);
  border-radius: 50%;
  background: rgba(48, 213, 200, 0.12);
  filter: blur(34px);
}

.orbit {
  position: absolute;
  top: 50%;
  left: 50%;
  border: 1px solid rgba(137, 221, 228, 0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.orbit-outer {
  width: 360px;
  height: 360px;
}
.orbit-inner {
  width: 260px;
  height: 260px;
  border-style: dashed;
}

.core-card {
  position: absolute;
  top: 50%;
  left: 50%;
  display: flex;
  width: 198px;
  height: 198px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.04));
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.18),
    0 30px 50px rgba(0, 20, 40, 0.28);
  backdrop-filter: blur(12px);
  transform: translate(-50%, -50%);
}

.core-icon {
  display: grid;
  width: 54px;
  height: 54px;
  margin-bottom: 15px;
  place-items: center;
  border-radius: 17px;
  color: #0b4a62;
  font-size: 27px;
  background: linear-gradient(135deg, #88eee1, #55cddd);
  box-shadow: 0 14px 28px rgba(61, 206, 200, 0.26);
}

.core-card strong {
  font-size: 16px;
}
.core-card > span {
  margin-top: 7px;
  color: rgba(223, 242, 250, 0.55);
  font-size: 10px;
  letter-spacing: 0.08em;
}

.float-card {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 138px;
  padding: 12px 15px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 14px;
  color: #eaf8ff;
  background: rgba(7, 38, 70, 0.68);
  box-shadow: 0 16px 28px rgba(0, 22, 45, 0.2);
  backdrop-filter: blur(12px);
}

.float-card > .el-icon {
  color: #6fe1d6;
  font-size: 21px;
}
.float-card div {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.float-card small {
  color: rgba(222, 241, 250, 0.56);
  font-size: 10px;
}
.float-card b {
  font-size: 12px;
}
.card-ledger {
  top: 61px;
  left: 3%;
}
.card-flow {
  top: 118px;
  right: 1%;
}
.card-security {
  right: 7%;
  bottom: 55px;
}

.value-grid {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin: -18px 26px 0;
}

.value-card {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 105px;
  padding: 22px;
  border: 1px solid #e7edf5;
  border-radius: 17px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 13px 35px rgba(31, 62, 98, 0.08);
}

.value-number {
  color: #167e91;
  font-size: 28px;
  font-weight: 750;
  letter-spacing: -0.04em;
}
.value-card h3 {
  margin: 0 0 6px;
  color: #1b2b45;
  font-size: 14px;
}
.value-card p {
  margin: 0;
  color: #8591a5;
  font-size: 11px;
  line-height: 1.55;
}

.content-panel,
.architecture-panel {
  margin-top: 22px;
  border: 1px solid #e5ebf3;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 12px 36px rgba(31, 55, 85, 0.06);
}

.content-panel {
  padding: 34px 38px 38px;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.section-kicker {
  color: #168fa0;
}
.section-kicker.light {
  color: #7ce2d8;
}
.section-heading h2,
.architecture-copy h2 {
  margin: 9px 0 8px;
  color: #172945;
  font-size: 23px;
  letter-spacing: -0.02em;
}
.section-heading p,
.architecture-copy p {
  margin: 0;
  color: #7b899e;
  font-size: 13px;
  line-height: 1.7;
}

.legend {
  display: flex;
  gap: 17px;
  flex-shrink: 0;
  color: #8490a2;
  font-size: 11px;
}
.legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.legend-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}
.legend-dot.ready {
  background: #23aea7;
}
.legend-dot.planned {
  border: 1px solid #a9b5c5;
  background: #fff;
}

.lifecycle-track {
  display: flex;
  align-items: flex-start;
  margin-top: 34px;
  overflow-x: auto;
  padding: 2px 2px 10px;
}

.lifecycle-step {
  display: flex;
  min-width: 66px;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.step-index {
  display: grid;
  width: 36px;
  height: 36px;
  margin-bottom: 11px;
  place-items: center;
  border: 1px solid #d2dbe7;
  border-radius: 11px;
  color: #97a3b5;
  font-size: 12px;
  font-weight: 700;
  background: #f7f9fc;
}

.lifecycle-step.ready .step-index {
  border-color: #31b9b2;
  color: #fff;
  background: linear-gradient(135deg, #29a7b8, #34c0a9);
  box-shadow: 0 9px 20px rgba(44, 177, 174, 0.2);
}

.lifecycle-step strong {
  color: #33445f;
  font-size: 13px;
}
.lifecycle-step small {
  margin-top: 5px;
  color: #9aa6b7;
  font-size: 10px;
  white-space: nowrap;
}
.step-line {
  min-width: 26px;
  height: 1px;
  flex: 1;
  margin: 18px 8px 0;
  background: #dfe5ed;
}
.step-line.ready {
  background: linear-gradient(90deg, #33b8af, #8fddd6);
}

.capability-section {
  margin-top: 35px;
}
.section-heading.compact {
  padding: 0 4px;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 21px;
}

.capability-card {
  min-height: 228px;
  padding: 23px;
  border: 1px solid #e5ebf3;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 9px 28px rgba(35, 58, 87, 0.055);
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    border-color 0.22s ease;
}

.capability-card:hover {
  border-color: #cfe5e7;
  box-shadow: 0 18px 40px rgba(29, 68, 94, 0.1);
  transform: translateY(-4px);
}

.capability-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.capability-icon {
  display: grid;
  width: 45px;
  height: 45px;
  place-items: center;
  border-radius: 13px;
  font-size: 21px;
}
.capability-icon.blue {
  color: #2471d8;
  background: #eaf3ff;
}
.capability-icon.cyan {
  color: #138f91;
  background: #e7f8f6;
}
.capability-icon.orange {
  color: #d67a24;
  background: #fff3e4;
}
.capability-icon.purple {
  color: #785fc1;
  background: #f1edff;
}
.status-tag {
  padding: 5px 9px;
  border-radius: 20px;
  font-size: 10px;
  font-weight: 600;
}
.status-tag.online {
  color: #16867f;
  background: #e8f8f5;
}
.status-tag.roadmap {
  color: #8a6a25;
  background: #fff6de;
}
.capability-card h3 {
  margin: 20px 0 10px;
  color: #263852;
  font-size: 16px;
}
.capability-card > p {
  min-height: 58px;
  margin: 0;
  color: #7f8ca0;
  font-size: 12px;
  line-height: 1.75;
}
.capability-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 17px;
}
.capability-tags span {
  padding: 5px 8px;
  border-radius: 6px;
  color: #66758b;
  font-size: 10px;
  background: #f3f6fa;
}

.architecture-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(330px, 0.7fr);
  gap: 36px;
  overflow: hidden;
  padding: 37px 42px;
  color: #fff;
  border: none;
  background: linear-gradient(120deg, rgba(10, 35, 67, 0.98), rgba(18, 69, 94, 0.96)), #0b2949;
}

.architecture-copy h2 {
  color: #fff;
}
.architecture-copy p {
  max-width: 700px;
  color: rgba(221, 237, 249, 0.66);
}
.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 22px;
}
.tech-tags span {
  padding: 7px 11px;
  border: 1px solid rgba(120, 220, 214, 0.18);
  border-radius: 8px;
  color: rgba(231, 246, 251, 0.78);
  font-size: 10px;
  background: rgba(255, 255, 255, 0.055);
}
.architecture-flow {
  align-self: center;
}
.arch-layer {
  padding: 13px 17px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.07);
}
.arch-layer small {
  display: block;
  margin-bottom: 4px;
  color: #6bd9d2;
  font-size: 9px;
  letter-spacing: 0.12em;
}
.arch-layer strong {
  color: rgba(244, 250, 255, 0.9);
  font-size: 12px;
}
.arch-arrow {
  height: 20px;
  color: rgba(103, 214, 207, 0.5);
  text-align: center;
}

.home-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 6px 8px;
  color: #8a96a8;
  font-size: 10px;
}

.home-footer > div {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.home-footer strong {
  color: #53637a;
  font-size: 11px;
}
.footer-mark {
  color: #208a94;
  font-weight: 600;
  letter-spacing: 0.08em;
}

@media (max-width: 1200px) {
  .hero-section {
    grid-template-columns: 1fr 390px;
  }
  .hero-copy {
    padding-left: 46px;
  }
  .capability-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .value-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 900px) {
  .asset-home {
    padding: 15px;
  }
  .hero-section {
    grid-template-columns: 1fr;
  }
  .hero-copy {
    padding: 45px 32px 28px;
  }
  .hero-visual {
    min-height: 380px;
  }
  .value-grid {
    margin: 14px 0 0;
  }
  .architecture-panel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .hero-copy {
    padding: 37px 22px 20px;
  }
  .hero-copy h1 {
    font-size: 30px;
  }
  .hero-description {
    font-size: 13px;
  }
  .hero-actions {
    align-items: stretch;
    flex-direction: column;
  }
  .hero-actions :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }
  .hero-assurances {
    gap: 10px 16px;
  }
  .hero-visual {
    min-height: 330px;
    transform: scale(0.88);
    transform-origin: top center;
  }
  .value-grid,
  .capability-grid {
    grid-template-columns: 1fr;
  }
  .value-card {
    min-height: 90px;
  }
  .content-panel {
    padding: 27px 20px;
  }
  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }
  .legend {
    align-self: flex-start;
  }
  .architecture-panel {
    padding: 30px 23px;
  }
  .home-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (prefers-reduced-motion: reduce) {
  .capability-card {
    transition: none;
  }
}
</style>
