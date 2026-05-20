# EBU6304 助教招聘系统 - 详细开发计划

## Git 提交规范

### 提交信息格式

```text
<type>: <description>

[optional body]
```

### Type 类型

- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试
- `perf`: 性能优化
- `chore`: 杂项

## 分阶段分工与进行

### 第一阶段：对应第一次评估 (3月22日) - 30%

**目标**: 完成产品待办列表、低保真原型、短报告

**时间安排**:

|活动|Git 提交|
|---|---|
|项目启动、需求调研、角色分工|`init: 初始化项目结构`|
|编写用户故事、创建产品待办列表|`docs: 添加用户故事和Product Backlog`|
|绘制低保真原型、撰写报告|`docs: 添加原型和报告`|

**交付物**:

- `ProductBacklog_groupXXX.xlsx` - 产品待办事项列表
- `Prototype_groupXXX.pdf` - 低保真原型 (手绘/PPT/草图)
- `Report_groupXXX.pdf` - 简短报告 (最多5页)

### 第二阶段：中期评估 (4月12日) - 20%

#### 成员1 (后端) - auth-login

负责：登录注册 + Session管理

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 创建User实体类和UserDao`|✅|`git commit -m "feat: 创建User实体类和UserDao" --author="member1 <member1@edu.com>"`|
|#2|`feat: 实现登录Servlet和注册Servlet`|✅|`git commit -m "feat: 实现登录Servlet和注册Servlet" --author="member1 <member1@edu.com>"`|
|#3|`feat: 添加Session管理和权限验证`|✅|`git commit -m "feat: 添加Session管理和权限验证" --author="member1 <member1@edu.com>"`|
|#4|`fix: 修复验证登录密码加密问题`|✅|`git commit -m "fix: 修复验证登录密码加密问题" --author="member1 <member1@edu.com>"`|
|#5|`refactor: 优化登录逻辑，添加错误处理`|✅|`git commit -m "refactor: 优化登录逻辑，添加错误处理" --author="member1 <member1@edu.com>"`|

---

#### 成员2 (后端) - applicant-profile

负责：TA档案创建 + 简历上传

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 创建Applicant实体类和ApplicantDao`|✅|`git commit -m "feat: 创建Applicant实体类和ApplicantDao" --author="member2 <member2@edu.com>"`|
|#2|`feat: 实现档案创建Servlet，支持基本信息存储`|✅|`git commit -m "feat: 实现档案创建Servlet，支持基本信息存储" --author="member2 <member2@edu.com>"`|
|#3|`feat: 实现简历文件上传功能`|✅|`git commit -m "feat: 实现简历文件上传功能" --author="member2 <member2@edu.com>"`|
|#4|`fix: 修复文件上传大小限制问题`|✅|`git commit -m "fix: 修复文件上传大小限制问题" --author="member2 <member2@edu.com>"`|
|#5|`refactor: 添加档案完整性验证`|✅|`git commit -m "refactor: 添加档案完整性验证" --author="member2 <member2@edu.com>"`|

---

#### 成员3 (后端) - job-posting

负责：MO发布职位 + 职位列表

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 创建Job职位实体类和JobDao`|✅|`git commit -m "feat: 创建Job职位实体类和JobDao" --author="member3 <member3@edu.com>"`|
|#2|`feat: 实现职位发布Servlet`|✅|`git commit -m "feat: 实现职位发布Servlet" --author="member3 <member3@edu.com>"`|
|#3|`feat: 实现职位列表查询API，支持筛选`|✅|`git commit -m "feat: 实现职位列表查询API，支持筛选" --author="member3 <member3@edu.com>"`|
|#4|`fix: 修复职位状态显示错误`|✅|`git commit -m "fix: 修复职位状态显示错误" --author="member3 <member3@edu.com>"`|
|#5|`feat: 添加职位编辑和删除功能`|✅|`git commit -m "feat: 添加职位编辑和删除功能" --author="member3 <member3@edu.com>"`|

---

#### 成员4 (后端) - application-status

负责：查看申请状态 + MO选择

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 创建Application申请实体类和ApplicationDao`|✅|`git commit -m "feat: 创建Application申请实体类和ApplicationDao" --author="member4 <member4@edu.com>"`|
|#2|`feat: 实现职位申请Servlet`|✅|`git commit -m "feat: 实现职位申请Servlet" --author="member4 <member4@edu.com>"`|
|#3|`feat: 实现申请状态查询API`|✅|`git commit -m "feat: 实现申请状态查询API" --author="member4 <member4@edu.com>"`|
|#4|`feat: 添加MO选择申请人功能`|✅|`git commit -m "feat: 添加MO选择申请人功能" --author="member4 <member4@edu.com>"`|
|#5|`fix: 修复状态更新不及时的问题`|✅|`git commit -m "fix: 修复状态更新不及时的问题" --author="member4 <member4@edu.com>"`|

---

#### 成员5 (前端) - auth-login + applicant-profile

负责：登录注册界面 + TA档案界面

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 设计并实现登录页面HTML/CSS`|✅|`git commit -m "feat: 设计并实现登录页面HTML/CSS" --author="member5 <member5@edu.com>"`|
|#2|`feat: 设计并实现注册页面`|✅|`git commit -m "feat: 设计并实现注册页面" --author="member5 <member5@edu.com>"`|
|#3|`feat: 设计并实现TA档案创建页面`|✅|`git commit -m "feat: 设计并实现TA档案创建页面" --author="member5 <member5@edu.com>"`|
|#4|`feat: 添加简历上传前端逻辑和进度显示`|✅|`git commit -m "feat: 添加简历上传前端逻辑和进度显示" --author="member5 <member5@edu.com>"`|
|#5|`style: 优化表单样式和用户体验`|✅|`git commit -m "style: 优化表单样式和用户体验" --author="member5 <member5@edu.com>"`|

---

#### 成员6 (前端) - job-posting + application-status

负责：职位浏览界面 + 申请状态界面

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 设计并实现职位列表页面`|✅|`git commit -m "feat: 设计并实现职位列表页面" --author="member6 <member6@edu.com>"`|
|#2|`feat: 设计并实现职位详情页面`|✅|`git commit -m "feat: 设计并实现职位详情页面" --author="member6 <member6@edu.com>"`|
|#3|`feat: 设计并实现MO发布职位页面`|✅|`git commit -m "feat: 设计并实现MO发布职位页面" --author="member6 <member6@edu.com>"`|
|#4|`feat: 设计并实现申请状态查看页面`|✅|`git commit -m "feat: 设计并实现申请状态查看页面" --author="member6 <member6@edu.com>"`|
|#5|`feat: 添加MO选择申请人的操作界面并作阶段性前端整体检查与优化`|✅|`git commit -m "feat: 添加MO选择申请人的操作界面并作阶段性前端整体检查与优化" --author="member6 <member6@edu.com>"`|

### 第三阶段：对应最终评估(5月24日) 中第三次迭代 - 50%

#### 成员1 (后端) - ai-skill-match

负责：AI技能匹配

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 创建SkillMatch服务类，定义技能匹配算法`|✅|`git commit -m "feat: 创建SkillMatch服务类，定义技能匹配算法" --author="member1 <member1@edu.com>"`|
|#2|`feat: 实现基于关键词的技能匹配逻辑`|✅|`git commit -m "feat: 实现基于关键词的技能匹配逻辑" --author="member1 <member1@edu.com>"`|
|#3|`feat: 集成AI API进行智能技能匹配`|✅|`git commit -m "feat: 集成AI API进行智能技能匹配" --author="member1 <member1@edu.com>"`|
|#4|`fix: 优化匹配算法性能，减少响应时间`|✅|`git commit -m "fix: 优化匹配算法性能，减少响应时间" --author="member1 <member1@edu.com>"`|
|#5|`refactor: 添加缓存机制提升匹配效率`|✅|`git commit -m "refactor: 添加缓存机制提升匹配效率" --author="member1 <member1@edu.com>"`|

---

#### 成员2 (后端) - ai-missing-skills

负责：AI识别缺失技能

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 创建MissingSkills分析服务类`|✅|`git commit -m "feat: 创建MissingSkills分析服务类" --author="member2 <member2@edu.com>"`|
|#2|`feat: 实现职位要求与申请人技能对比逻辑`|✅|`git commit -m "feat: 实现职位要求与申请人技能对比逻辑" --author="member2 <member2@edu.com>"`|
|#3|`feat: 生成缺失技能报告和建议`|✅|`git commit -m "feat: 生成缺失技能报告和建议" --author="member2 <member2@edu.com>"`|
|#4|`fix: 修复技能对比边界条件错误`|✅|`git commit -m "fix: 修复技能对比边界条件错误" --author="member2 <member2@edu.com>"`|
|#5|`feat: 添加缺失技能可视化数据接口`|✅|`git commit -m "feat: 添加缺失技能可视化数据接口" --author="member2 <member2@edu.com>"`|

---

#### 成员3 (后端) - admin-workload

负责：管理员工作量统计

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 创建WorkloadStats统计服务类`|✅|`git commit -m "feat: 创建WorkloadStats统计服务类" --author="member3 <member3@edu.com>"`|
|#2|`feat: 实现申请数量统计API`|✅|`git commit -m "feat: 实现申请数量统计API" --author="member3 <member3@edu.com>"`|
|#3|`feat: 实现MO处理工作量统计`|✅|`git commit -m "feat: 实现MO处理工作量统计" --author="member3 <member3@edu.com>"`|
|#4|`feat: 添加时间段筛选和导出功能`|✅|`git commit -m "feat: 添加时间段筛选和导出功能" --author="member3 <member3@edu.com>"`|
|#5|`perf: 优化大数据量统计查询性能`|✅|`git commit -m "perf: 优化大数据量统计查询性能" --author="member3 <member3@edu.com>"`|

---

#### 成员4 (后端) - 集成测试 + 打包 + 用户手册

负责：集成测试、打包、用户手册

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`test: 编写登录注册模块集成测试`|✅|`git commit -m "test: 编写登录注册模块集成测试" --author="member4 <member4@edu.com>"`|
|#2|`test: 编写档案和职位模块集成测试`|✅|`git commit -m "test: 编写档案和职位模块集成测试" --author="member4 <member4@edu.com>"`|
|#3|`test: 编写申请流程端到端测试`|✅|`git commit -m "test: 编写申请流程端到端测试" --author="member4 <member4@edu.com>"`|
|#4|`chore: 配置Maven打包脚本，生成WAR文件`|✅|`git commit -m "chore: 配置Maven打包脚本，生成WAR文件" --author="member4 <member4@edu.com>"`|
|#5|`docs: 编写完整用户手册`|✅|`git commit -m "docs: 编写完整用户手册" --author="member4 <member4@edu.com>"`|

---

#### 成员5 (前端) - ai-skill-match + ai-missing-skills

负责：AI技能匹配界面 + AI缺失技能展示界面

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 设计技能匹配结果展示页面`|✅|`git commit -m "feat: 设计技能匹配结果展示页面" --author="member5 <member5@edu.com>"`|
|#2|`feat: 添加匹配度可视化组件`|✅|`git commit -m "feat: 添加匹配度可视化组件" --author="member5 <member5@edu.com>"`|
|#3|`feat: 设计缺失技能展示页面`|✅|`git commit -m "feat: 设计缺失技能展示页面" --author="member5 <member5@edu.com>"`|
|#4|`feat: 添加技能对比图表`|✅|`git commit -m "feat: 添加技能对比图表" --author="member5 <member5@edu.com>"`|
|#5|`style: 统一AI功能模块的UI风格`|✅|`git commit -m "style: 统一AI功能模块的UI风格" --author="member5 <member5@edu.com>"`|

---

#### 成员6 (前端) - admin-workload + responsive + 演示视频

负责：管理统计仪表盘 + 响应式设计 + 演示视频

|Commit|Message|状态|Git Command|
|---|---|---|---|
|#1|`feat: 设计管理员统计仪表盘`|✅|`git commit -m "feat: 设计管理员统计仪表盘" --author="member6 <member6@edu.com>"`|
|#2|`feat: 实现数据可视化图表`|✅|`git commit -m "feat: 实现数据可视化图表" --author="member6 <member6@edu.com>"`|
|#3|`feat: 添加响应式布局适配移动端`|✅|`git commit -m "feat: 添加响应式布局适配移动端" --author="member6 <member6@edu.com>"`|
|#4|`style: 优化响应式样式细节`|✅|`git commit -m "style: 优化响应式样式细节" --author="member6 <member6@edu.com>"`|
|#5|`refactor: 整体检查并优化前端代码质量`|✅|`git commit -m "refactor: 整体检查并优化前端代码质量" --author="member6 <member6@edu.com>"`|

### 第四阶段：对应最终评估(5月24日) 中第四次迭代 - 50%

- 随机应变，主要解决使用与测试中各种遗留小问题
