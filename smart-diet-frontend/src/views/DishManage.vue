<template>
  <div class="content-container section-gap">
    <el-card class="dish-manage-card">
      <!-- 统一的页面头部修饰栏 -->
      <div class="panel-header-section">
        <h3 class="page-title">
          <el-icon class="title-icon">
            <ForkSpoon/>
          </el-icon>
          健康菜谱管理
        </h3>
        <span class="sub-title">系统膳食健康菜谱管理，维护多做法分支、原辅食材用量及详细加工烹饪步骤</span>
      </div>

      <!-- 搜索与操作栏（合并为单行展示） -->
      <div class="search-bar" style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap;">
        <el-form :inline="true" @submit.prevent style="margin-bottom: 0;">
          <el-form-item label="菜谱名称">
            <el-input
                v-model="searchName"
                placeholder="搜索菜谱名称..."
                clearable
                @keyup.enter="handleSearch"
                style="width: 160px;"
            />
          </el-form-item>
          <el-form-item label="菜系">
            <el-select v-model="searchCuisine" placeholder="全部菜系" style="width: 120px" clearable @change="handleSearch">
              <el-option
                  v-for="item in allCuisineTypes"
                  :key="item.dataCode"
                  :label="item.dataValue"
                  :value="item.dataCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="膳食模式">
            <el-select v-model="filterMode" placeholder="膳食模式" style="width: 120px" @change="handleSearch">
              <el-option :value="null" label="全部模式"/>
              <el-option :value="0" label="正常饮食"/>
              <el-option :value="1" label="轻食减脂"/>
              <el-option :value="2" label="放纵餐"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="action-buttons">
          <el-button type="primary" @click="openCreateModal">
            <el-icon style="margin-right: 4px;">
              <Plus/>
            </el-icon>
            新建健康菜谱
          </el-button>
        </div>
      </div>

      <!-- 菜谱列表 (点击做法弹出 Drawer 渲染用料和步骤) -->
      <el-table
          v-loading="loading"
          :data="dishes"
          border
          max-height="calc(100vh - 240px)"
          style="width: 100%; margin-top: 10px"
      >
        <el-table-column prop="dishId" label="ID" width="80" align="center"/>
        <el-table-column label="预览图" width="70" align="center">
          <template #default="scope">
            <el-image
                :src="scope.row.previewUrl || 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=500'"
                :preview-src-list="[scope.row.previewUrl || 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=500']"
                preview-teleported
                fit="cover"
                class="dish-preview-thumb animate-thumb"
                style="width: 40px; height: 40px; border-radius: 4px; display: block; margin: 0 auto; cursor: pointer;"
            />
          </template>
        </el-table-column>
        <el-table-column label="主菜品" min-width="150">
          <template #default="scope">
            <strong class="dish-title-text">{{ scope.row.dishName }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="做法" min-width="140" align="left">
          <template #default="scope">
            <el-tag size="small" type="success" effect="plain" class="mini-branch-tag">
              {{ scope.row.branchName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cuisineTypeStr" label="菜系" width="85" align="center"/>
        <el-table-column prop="dietModeStr" label="就餐建议" width="100" align="center"/>
        <el-table-column label="热量 (100g)" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.calories || 0 }}</span> <span class="unit">kcal</span>
          </template>
        </el-table-column>
        <el-table-column label="蛋白质 (100g)" width="130" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.protein || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="脂肪 (100g)" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.fat || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="碳水 (100g)" width="110" align="right">
          <template #default="scope">
            <span class="font-mono">{{ scope.row.carbs || 0 }}</span> <span class="unit">g</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="showDishDetailDrawer(scope.row.dishId, scope.row.branchId)">查看做法</el-button>
            <el-button type="primary" link @click="openEditModal(scope.row.dishId, scope.row.branchId)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.branchId, scope.row.dishName + ' (' + scope.row.branchName + ')')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 真分页器 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            :total="totalCount"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 做法详情与用料配方抽屉 (Drawer) -->
    <el-drawer
        v-model="drawerVisible"
        :title="`做法与用料配方 - ${currentDetailDish?.dishName || ''}`"
        size="620px"
        direction="rtl"
        custom-class="premium-drawer"
    >
      <div v-if="drawerLoading" class="loading-state">
        <div class="spinner"></div>
        <p class="body-sm text-muted">正在加载做法配方数据...</p>
      </div>
      <div v-else-if="currentDetailDish" class="drawer-detail-content">
        <!-- 做法分支或主菜品封面图 -->
        <div class="detail-section" style="margin-bottom: 24px; text-align: center;" v-if="currentDetailDish.previewUrl">
          <el-image
              :src="currentDetailDish.previewUrl"
              :preview-src-list="[currentDetailDish.previewUrl]"
              fit="cover"
              style="width: 100%; max-height: 240px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);"
          />
        </div>

        <!-- 核心营养大盘卡片 -->
        <div class="metrics-summary-card color-block color-block-mint" style="margin-bottom: 24px;">
          <span class="eyebrow" style="color: #27a644;">NUTRITION SUMMARY (每100克成品)</span>
          <div class="metrics-row font-mono" style="display: flex; justify-content: space-between; margin-top: 14px;">
            <div class="metric-block">
              <span class="label">热量</span>
              <div class="val">{{ currentDetailDish.calories || 0 }} <span class="unit">kcal</span></div>
            </div>
            <div class="metric-block">
              <span class="label">蛋白质</span>
              <div class="val">{{ currentDetailDish.protein || 0 }} <span class="unit">g</span></div>
            </div>
            <div class="metric-block">
              <span class="label">脂肪</span>
              <div class="val">{{ currentDetailDish.fat || 0 }} <span class="unit">g</span></div>
            </div>
            <div class="metric-block">
              <span class="label">碳水</span>
              <div class="val">{{ currentDetailDish.carbs || 0 }} <span class="unit">g</span></div>
            </div>
          </div>
        </div>

        <!-- 原料配方清单 -->
        <div class="detail-section" style="margin-bottom: 28px;">
          <h3 class="section-sub-title">🥗 原材料配方</h3>
          <div class="materials-list-view">
            <div v-for="ing in currentDetailIngredients" :key="ing.ingredientId" class="material-item-row">
              <span class="m-name">{{ ing.ingredientName }}</span>
              <div class="m-meta-info" style="display: flex; align-items: center;">
                <el-tag size="small" :type="ing.mainMaterialFlag === 1 ? 'danger' : 'info'" style="margin-right: 12px; border-radius: 4px;">
                  {{ ing.mainMaterialFlag === 1 ? '主料' : '辅料' }}
                </el-tag>
                <span class="m-amount font-mono" style="font-weight: 600;">{{ ing.useAmount }}{{ ing.measureUnit || 'g' }}</span>
              </div>
            </div>
            <div v-if="currentDetailIngredients.length === 0" class="empty-tip-small card-empty-style">暂无配方原料</div>
          </div>
        </div>

        <!-- 制作步骤工序 -->
        <div class="detail-section">
          <h3 class="section-sub-title">🍳 烹饪工序步骤</h3>
          <div class="steps-list-view">
            <div v-for="(step, index) in currentDetailSteps" :key="index" class="step-item-view">
              <div class="step-header">
                <span class="step-num font-mono">STEP {{ index + 1 }}</span>
              </div>
              <div class="step-content">
                <p class="step-desc">{{ step.stepDetail || step.customDetail }}</p>
                <div class="step-meta-info" v-if="step.durationSeconds !== undefined || step.firePowerLabel">
                  <span class="meta-tag fire" v-if="step.firePowerLabel">
                    <span class="meta-icon">🔥</span>{{ step.firePowerLabel }}
                  </span>
                  <span class="meta-tag duration" v-if="step.durationSeconds !== undefined && step.durationSeconds !== null">
                    <span class="meta-icon">⏱️</span>{{ step.durationSeconds }} 秒
                  </span>
                </div>
              </div>
            </div>
            <div v-if="currentDetailSteps.length === 0" class="empty-tip-small card-empty-style">暂无烹饪工序步骤</div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 菜谱配置 Dialog（基础属性 + 分支独立用料步骤） -->
    <el-dialog
        v-model="modalVisible"
        :title="form.dishId ? '编辑健康菜谱' : '新建健康菜谱'"
        width="980px"
        custom-class="premium-dialog"
    >
      <el-tabs v-model="activeTab" class="premium-tabs">
        <!-- Tab 1: 主菜品基本配置 -->
        <el-tab-pane label="主菜品基础信息" name="basic">
          <div class="dish-editor-scrollable">
            <div class="section-title">
              <span class="eyebrow">BASIC INFORMATION</span>
              <h3>1. 主菜品属性</h3>
            </div>
            <div class="editor-grid">
              <div class="form-row-2">
                <div class="form-item">
                  <label class="caption">主菜品名称</label>
                  <input type="text" v-model="form.dishName" class="input-text" placeholder="例如: 西兰花炒牛肉"/>
                </div>
                <div class="form-item">
                  <label class="caption">菜系类型</label>
                  <el-select v-model="form.cuisineType" placeholder="选择菜系" style="width: 100%">
                    <el-option
                        v-for="item in allCuisineTypes"
                        :key="item.dataCode"
                        :label="item.dataValue"
                        :value="item.dataCode"
                    />
                  </el-select>
                </div>
              </div>

              <div class="form-row-2">
                <div class="form-item">
                  <label class="caption">建议就餐膳食模式</label>
                  <el-select v-model="form.dietMode" placeholder="选择建议模式" style="width: 100%">
                    <el-option :value="0" label="正常饮食 (常规膳食，老少咸宜)"/>
                    <el-option :value="1" label="轻食减脂 (少油低卡，利于控制能量)"/>
                    <el-option :value="2" label="放纵餐 (高碳高热，欺骗餐模式)"/>
                  </el-select>
                </div>
                <div class="form-item">
                  <label class="caption" style="margin-bottom: 6px;">自制成品图上传</label>
                  <el-upload
                      v-model:file-list="fileList"
                      :action="`${BASE_URL}/api/s3Storage/upload`"
                      :headers="uploadHeaders"
                      :data="{ bucketName: 'file' }"
                      list-type="picture-card"
                      :limit="1"
                      :on-success="handleUploadSuccess"
                      :on-remove="handleRemove"
                  >
                    <el-icon>
                      <Plus/>
                    </el-icon>
                  </el-upload>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 多烹饪做法分支配置 -->
        <el-tab-pane label="烹饪做法分支与用料工序" name="branches">
          <div class="branch-editor-layout">
            <!-- 左侧分支选择 -->
            <div class="branch-list-sidebar">
              <div class="sidebar-header">
                <span>做法分支列表</span>
                <el-button size="small" type="primary" plain @click="addNewBranch">添加做法</el-button>
              </div>
              <div class="sidebar-body">
                <div
                    v-for="(b, idx) in form.branches"
                    :key="idx"
                    :class="['branch-sidebar-item', { active: activeBranchIdx === Number(idx) }]"
                    @click="activeBranchIdx = Number(idx)"
                >
                  <span class="b-name">🥢 {{ b.branchName || '未命名做法' }}</span>
                  <el-icon class="delete-icon" @click.stop="removeBranch(Number(idx))">
                    <Close/>
                  </el-icon>
                </div>
                <div v-if="form.branches.length === 0" class="sidebar-empty">
                  暂无做法分支，请添加做法
                </div>
              </div>
            </div>

            <!-- 右侧分支配置表单 -->
            <div class="branch-detail-form" v-if="form.branches[activeBranchIdx]">
              <div class="dish-editor-scrollable" style="max-height: 52vh; padding-right: 16px;">
                <!-- 做法名称及热量模式 -->
                <div class="editor-grid">
                  <div class="form-row-2">
                    <div class="form-item">
                      <label class="caption">分支做法名称</label>
                      <input
                          type="text"
                          v-model="form.branches[activeBranchIdx].branchName"
                          class="input-text"
                          placeholder="例如: 减脂健身版、微辣多酱版"
                      />
                    </div>
                    <div class="form-item">
                      <label class="caption">营养指标设置</label>
                      <div style="margin-top: 8px;">
                        <el-checkbox v-model="form.branches[activeBranchIdx].autoCalculateNutrients">
                          自动核算（基于原料配置及其重量折算）
                        </el-checkbox>
                      </div>
                    </div>
                  </div>
                  <!-- 新增做法专属菜系与模式选择 -->
                  <div class="form-row-2">
                    <div class="form-item">
                      <label class="caption">做法菜系类型</label>
                      <el-select v-model="form.branches[activeBranchIdx].cuisineType" placeholder="做法菜系" style="width: 100%">
                        <el-option
                            v-for="item in allCuisineTypes"
                            :key="item.dataCode"
                            :label="item.dataValue"
                            :value="item.dataCode"
                        />
                      </el-select>
                    </div>
                    <div class="form-item">
                      <label class="caption">做法建议膳食模式</label>
                      <el-select v-model="form.branches[activeBranchIdx].dietMode" placeholder="做法膳食模式" style="width: 100%">
                        <el-option :value="0" label="正常饮食 (常规膳食，老少咸宜)"/>
                        <el-option :value="1" label="轻食减脂 (少油低卡，利于控制能量)"/>
                        <el-option :value="2" label="放纵餐 (高碳高热，欺骗餐模式)"/>
                      </el-select>
                    </div>
                  </div>

                  <!-- 做法分支封面图片上传 -->
                  <div class="form-row-2">
                    <div class="form-item">
                      <label class="caption" style="margin-bottom: 6px;">做法分支封面图片</label>
                      <el-upload
                          v-model:file-list="form.branches[activeBranchIdx].fileList"
                          :action="`${BASE_URL}/api/s3Storage/upload`"
                          :headers="uploadHeaders"
                          :data="{ bucketName: 'file' }"
                          list-type="picture-card"
                          :limit="1"
                          :on-success="(res: any, file: any) => handleBranchUploadSuccess(res, file, form.branches[activeBranchIdx])"
                          :on-remove="() => handleBranchRemove(form.branches[activeBranchIdx])"
                      >
                        <el-icon>
                          <Plus/>
                        </el-icon>
                      </el-upload>
                    </div>
                  </div>

                  <!-- 自动核算提示或手动录入 -->
                  <div v-if="form.branches[activeBranchIdx].autoCalculateNutrients" class="alert-info-box">
                    <span class="info-icon">💡</span>
                    <p class="caption-desc text-info-highlight">
                      <strong>已开启智能核算：</strong>系统将根据您在下方配料表中选择的食材和精确克数，自动匹配数据库计算出本做法的热量及碳水、蛋白质、脂肪。
                    </p>
                  </div>
                  <div class="form-row-4" v-else>
                    <div class="form-item">
                      <label class="caption">热量 (kcal/100g)</label>
                      <input type="number" v-model="form.branches[activeBranchIdx].calories" class="input-text" step="0.1"/>
                    </div>
                    <div class="form-item">
                      <label class="caption">蛋白质 (g/100g)</label>
                      <input type="number" v-model="form.branches[activeBranchIdx].protein" class="input-text" step="0.1"/>
                    </div>
                    <div class="form-item">
                      <label class="caption">脂肪 (g/100g)</label>
                      <input type="number" v-model="form.branches[activeBranchIdx].fat" class="input-text" step="0.1"/>
                    </div>
                    <div class="form-item">
                      <label class="caption">碳水 (g/100g)</label>
                      <input type="number" v-model="form.branches[activeBranchIdx].carbs" class="input-text" step="0.1"/>
                    </div>
                  </div>
                </div>

                <!-- 分支专属食材 -->
                <div class="section-title" style="margin-top: 24px; border-bottom: 2px solid var(--hairline-strong);">
                  <div class="sub-header-row">
                    <h3 style="font-size: 16px; font-weight: 700; display: flex; align-items: center; gap: 6px;">
                      <span>🥗</span> 配料清单
                    </h3>
                    <el-button size="small" type="primary" plain @click="addIngredientRow">+ 添加食材</el-button>
                  </div>
                </div>

                <div class="ingredients-list-editor">
                  <div v-for="(row, ingIdx) in form.branches[activeBranchIdx].ingredients" :key="ingIdx" class="dynamic-row-card">
                    <div class="row-col" style="flex: 2.2;">
                      <span class="caption input-label-caption">选用食材</span>
                      <el-select v-model="row.ingredientId" filterable placeholder="检索原料..." style="width: 100%">
                        <el-option
                            v-for="ing in allIngredients"
                            :key="ing.ingredientId"
                            :label="ing.ingredientName"
                            :value="ing.ingredientId"
                        />
                      </el-select>
                    </div>
                    <div class="row-col" style="flex: 1.2;">
                      <span class="caption input-label-caption">用量</span>
                      <div class="input-unit-wrapper">
                        <input type="number" v-model="row.useAmount" class="input-text" placeholder="用量"/>
                        <span class="unit-tag">{{ getUnit(row.ingredientId) }}</span>
                      </div>
                    </div>
                    <div class="row-col" style="flex: 1.4;">
                      <span class="caption input-label-caption">就餐配料角色</span>
                      <el-select v-model="row.mainMaterialFlag" style="width: 100%">
                        <el-option :value="1" label="核心主料"/>
                        <el-option :value="0" label="辅料调味"/>
                      </el-select>
                    </div>
                    <div class="row-col align-center" style="flex: 0 0 32px; display: flex; justify-content: center; margin-top: 14px;">
                      <button class="btn-delete-row" @click="removeIngredientRow(Number(ingIdx))" title="删除">✕</button>
                    </div>
                  </div>
                  <div v-if="form.branches[activeBranchIdx].ingredients.length === 0" class="sidebar-empty card-empty-style">
                    👈 点击右上角“添加食材”按钮，开始配置本做法的配方原料
                  </div>
                </div>

                <!-- 分支专属步骤 -->
                <div class="section-title" style="margin-top: 28px; border-bottom: 2px solid var(--hairline-strong);">
                  <div class="sub-header-row">
                    <h3 style="font-size: 16px; font-weight: 700; display: flex; align-items: center; gap: 6px;">
                      <span>🍳</span> 加工与烹饪工序
                    </h3>
                    <el-button size="small" type="primary" plain @click="addStepRow">+ 新增步骤</el-button>
                  </div>
                </div>

                <div class="steps-rows-container">
                  <div v-for="(step, sIdx) in form.branches[activeBranchIdx].steps" :key="sIdx" class="step-card-editor hairline-border">
                    <div class="step-card-header">
                      <div class="step-num-badge">
                        <span class="step-num font-mono">STEP {{ Number(sIdx) + 1 }}</span>
                      </div>
                      <div class="step-template-select-col">
                        <span class="caption" style="font-weight: 600;">工序模板:</span>
                        <el-select
                            v-model="step.stepPoolId"
                            placeholder="可选预置工序..."
                            style="width: 160px; margin-left: 8px"
                            clearable
                            @change="(val: any) => handleStepTemplateChange(val, Number(sIdx))"
                        >
                          <el-option
                              v-for="pool in allStandardSteps"
                              :key="pool.stepPoolId"
                              :label="pool.stepName"
                              :value="pool.stepPoolId"
                          />
                        </el-select>
                      </div>
                      <div class="step-actions-col">
                        <button class="btn-step-act" :disabled="Number(sIdx) === 0" @click="moveStepUp(Number(sIdx))">▲</button>
                        <button class="btn-step-act" :disabled="Number(sIdx) === form.branches[activeBranchIdx].steps.length - 1" @click="moveStepDown(Number(sIdx))">▼</button>
                        <button class="btn-step-act text-danger" @click="removeStepRow(Number(sIdx))">✕</button>
                      </div>
                    </div>
                    <div class="step-card-body">
                      <textarea
                          v-model="step.customDetail"
                          class="textarea-editor"
                          rows="2"
                          placeholder="请在此输入本步骤的做法详情描述..."
                      ></textarea>
                      <div class="step-meta-row" style="display: flex; gap: 16px; margin-top: 10px; align-items: center;">
                        <div style="display: flex; align-items: center; gap: 8px; flex: 1;">
                          <span class="caption" style="white-space: nowrap; font-weight: 600; font-size: 13px;">烹饪时长 (秒):</span>
                          <el-input-number
                              v-model="step.durationSeconds"
                              :min="0"
                              :step="10"
                              size="small"
                              style="width: 100%;"
                          />
                        </div>
                        <div style="display: flex; align-items: center; gap: 8px; flex: 1;">
                          <span class="caption" style="white-space: nowrap; font-weight: 600; font-size: 13px;">推荐火候:</span>
                          <el-select
                              v-model="step.firePower"
                              placeholder="选择火候"
                              size="small"
                              style="width: 100%;"
                          >
                            <el-option
                                v-for="item in allFirePowers"
                                :key="item.dataCode"
                                :label="item.dataValue"
                                :value="Number(item.dataCode)"
                            />
                          </el-select>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-if="form.branches[activeBranchIdx].steps.length === 0" class="sidebar-empty card-empty-style">
                    👈 点击右上角“新增步骤”按钮，规划该做法的分步工序
                  </div>
                </div>

              </div>
            </div>
            <div class="branch-detail-empty" v-else>
              👈 请在左侧选择或新建一个烹饪做法，配置用料配方与详细步骤
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <button class="btn-secondary" style="margin-right: 10px" @click="modalVisible = false">取消</button>
        <button class="btn-primary" @click="handleSave">保存菜谱并自动核算</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Close, ForkSpoon, Plus} from '@element-plus/icons-vue'
import request from '../utils/request'
import {API_BASE_URL} from '../config'

const loading = ref(false)
const dishes = ref<any[]>([])
const searchName = ref('')
const searchCuisine = ref('')
const filterMode = ref<number | null>(null)
const modalVisible = ref(false)
const activeTab = ref('basic')
const activeBranchIdx = ref(0)

// 分页相关变量
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)

// 做法详情抽屉相关变量
const drawerVisible = ref(false)
const drawerLoading = ref(false)
const currentDetailDish = ref<any>(null)
const currentDetailIngredients = ref<any[]>([])
const currentDetailSteps = ref<any[]>([])

// 基础配置
const BASE_URL = API_BASE_URL
const fileList = ref<any[]>([])
const allCuisineTypes = ref<any[]>([])
const allIngredients = ref<any[]>([])
const allStandardSteps = ref<any[]>([])
const allFirePowers = ref<any[]>([])

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? {Authorization: 'Bearer ' + token} : {}
})

// 默认表单树形结构
const defaultForm = {
  dishId: null as number | null,
  dishName: '',
  cuisineType: '',
  dietMode: 0,
  coverImageId: null as number | null,
  branches: [] as any[]
}
const form = ref<any>({...defaultForm})

// 加载菜谱列表 (真分页)
const loadDishes = async () => {
  loading.value = true
  try {
    const res: any = await request.post('/api/dish/page', {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      dishName: (searchName.value && searchName.value.trim()) ? searchName.value.trim() : null,
      cuisineType: (searchCuisine.value && searchCuisine.value.trim()) ? searchCuisine.value.trim() : null,
      dietMode: filterMode.value !== null ? filterMode.value : null
    })

    // page 接口已被拦截器放行返回完整 Result 对象
    if (res && res.code === 200) {
      const records = res.data || []
      totalCount.value = res.page ? res.page.total : records.length

      // 向下兼容处理分支展示
      dishes.value = records.map((dish: any) => {
        if (!dish.branches || dish.branches.length === 0) {
          dish.branches = [
            {
              branchId: null,
              branchName: '经典传统做法',
              creatorName: '系统生成',
              calories: dish.calories,
              likes: 12,
              collects: 4,
              ingredients: dish.ingredients || [],
              steps: dish.steps || []
            }
          ]
        }
        return dish
      })
    }
  } catch (e) {
    console.error('获取菜谱列表失败', e)
  } finally {
    loading.value = false
  }
}

// 检索事件
const handleSearch = () => {
  currentPage.value = 1
  loadDishes()
}

// 重置检索
const resetSearch = () => {
  searchName.value = ''
  searchCuisine.value = ''
  filterMode.value = null
  currentPage.value = 1
  loadDishes()
}

// 分页切换
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadDishes()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadDishes()
}

// 获取详情并打开 Drawer
const showDishDetailDrawer = async (dishId: number, branchId: number) => {
  drawerVisible.value = true
  drawerLoading.value = true
  currentDetailDish.value = null
  currentDetailIngredients.value = []
  currentDetailSteps.value = []
  try {
    const res = await request.get(`/api/dish/detail?dishId=${dishId}`)
    if (res) {
      currentDetailDish.value = res.dish || null
      // 筛选当前行所关联的特定做法分支
      const currentBranch = (res.branches || []).find((b: any) => b.branchId === branchId)
      if (currentBranch) {
        currentDetailDish.value.dishName = `${res.dish.dishName} (${currentBranch.branchName})`
        currentDetailDish.value.calories = currentBranch.calories
        currentDetailDish.value.protein = currentBranch.protein
        currentDetailDish.value.fat = currentBranch.fat
        currentDetailDish.value.carbs = currentBranch.carbs
        currentDetailIngredients.value = currentBranch.ingredients || []
        currentDetailSteps.value = currentBranch.steps || []
        // 设置抽屉中展示的做法封面图，如果分支没有封面图，则回退到主菜品的封面图
        currentDetailDish.value.previewUrl = currentBranch.previewUrl || (res.dish.coverImageId ? `${BASE_URL}/api/s3Storage/preview/${res.dish.coverImageId}` : null)
      } else {
        currentDetailIngredients.value = res.ingredients || []
        currentDetailSteps.value = res.steps || []
        currentDetailDish.value.previewUrl = res.dish.coverImageId ? `${BASE_URL}/api/s3Storage/preview/${res.dish.coverImageId}` : null
      }
    }
  } catch (e) {
    console.error('获取菜品做法配方失败', e)
  } finally {
    drawerLoading.value = false
  }
}

const loadDicts = async () => {
  try {
    allIngredients.value = await request.get('/api/ingredient/list')
    allStandardSteps.value = await request.get('/api/step/list')

    // 统一通过 /sys/dict/more 接口批量加载数据字典 cuisine_type 和 fire_power_type
    try {
      const dictsRes: any = await request.get('/sys/dict/more?dataTypes=cuisine_type,fire_power_type')
      const dictMap = dictsRes.data || {}

      allCuisineTypes.value = dictMap.cuisine_type || [
        {dataCode: '1', dataValue: '川菜'},
        {dataCode: '2', dataValue: '粤菜'},
        {dataCode: '3', dataValue: '湘菜'},
        {dataCode: '4', dataValue: '鲁菜'},
        {dataCode: '5', dataValue: '闽菜'}
      ]

      allFirePowers.value = dictMap.fire_power_type || [
        {dataCode: '0', dataValue: '非热处理'},
        {dataCode: '1', dataValue: '小火/慢炖'},
        {dataCode: '2', dataValue: '中火/清蒸'},
        {dataCode: '3', dataValue: '大火/爆炒'}
      ]
    } catch (dictErr) {
      console.error('批量加载数据字典失败，使用本地默认值', dictErr)
      allCuisineTypes.value = [
        {dataCode: '1', dataValue: '川菜'},
        {dataCode: '2', dataValue: '粤菜'},
        {dataCode: '3', dataValue: '湘菜'},
        {dataCode: '4', dataValue: '鲁菜'},
        {dataCode: '5', dataValue: '闽菜'}
      ]
      allFirePowers.value = [
        {dataCode: '0', dataValue: '非热处理'},
        {dataCode: '1', dataValue: '小火/慢炖'},
        {dataCode: '2', dataValue: '中火/清蒸'},
        {dataCode: '3', dataValue: '大火/爆炒'}
      ]
    }
  } catch (e) {
    console.error('加载词典/基础数据失败', e)
  }
}

const getUnit = (ingredientId: number) => {
  const ing = allIngredients.value.find(i => i.ingredientId === ingredientId)
  return ing ? ing.measureUnit : 'g'
}


// 做法分支操作
const addNewBranch = () => {
  form.value.branches.push({
    branchId: null,
    branchName: `做法分支 ${form.value.branches.length + 1}`,
    cuisineType: form.value.cuisineType || '',
    dietMode: form.value.dietMode || 0,
    coverImageId: null,
    fileList: [],
    calories: 0,
    protein: 0,
    fat: 0,
    carbs: 0,
    autoCalculateNutrients: true,
    ingredients: [],
    steps: []
  })
  activeBranchIdx.value = form.value.branches.length - 1
}

const removeBranch = (idx: number) => {
  form.value.branches.splice(idx, 1)
  if (activeBranchIdx.value >= form.value.branches.length) {
    activeBranchIdx.value = Math.max(0, form.value.branches.length - 1)
  }
}

// 分支食材操作
const addIngredientRow = () => {
  if (!form.value.branches[activeBranchIdx.value]) return
  form.value.branches[activeBranchIdx.value].ingredients.push({
    ingredientId: null,
    useAmount: 100,
    mainMaterialFlag: 1
  })
}

const removeIngredientRow = (ingIdx: number) => {
  if (!form.value.branches[activeBranchIdx.value]) return
  form.value.branches[activeBranchIdx.value].ingredients.splice(ingIdx, 1)
}

// 分支步骤操作
const addStepRow = () => {
  if (!form.value.branches[activeBranchIdx.value]) return
  const steps = form.value.branches[activeBranchIdx.value].steps
  steps.push({
    stepPoolId: null,
    stepNum: steps.length + 1,
    customDetail: '',
    durationSeconds: 60,
    firePower: 0
  })
}

const removeStepRow = (sIdx: number) => {
  if (!form.value.branches[activeBranchIdx.value]) return
  const steps = form.value.branches[activeBranchIdx.value].steps
  steps.splice(sIdx, 1)
  steps.forEach((s: any, index: number) => {
    s.stepNum = index + 1
  })
}

const moveStepUp = (idx: number) => {
  if (idx === 0) return
  const steps = form.value.branches[activeBranchIdx.value].steps
  const temp = steps[idx]
  steps[idx] = steps[idx - 1]
  steps[idx - 1] = temp
  steps.forEach((s: any, i: number) => {
    s.stepNum = i + 1
  })
}

const moveStepDown = (idx: number) => {
  const steps = form.value.branches[activeBranchIdx.value].steps
  if (idx === steps.length - 1) return
  const temp = steps[idx]
  steps[idx] = steps[idx + 1]
  steps[idx + 1] = temp
  steps.forEach((s: any, i: number) => {
    s.stepNum = i + 1
  })
}

const handleStepTemplateChange = (val: any, sIdx: number) => {
  if (!val) return
  const steps = form.value.branches[activeBranchIdx.value].steps
  const template = allStandardSteps.value.find(p => p.stepPoolId === val)
  if (template) {
    steps[sIdx].customDetail = template.stepDetail
  }
}

// 对话框开启
const openCreateModal = () => {
  fileList.value = []
  activeTab.value = 'basic'
  activeBranchIdx.value = 0
  form.value = {
    dishId: null,
    dishName: '',
    cuisineType: '',
    dietMode: 0,
    coverImageId: null,
    branches: [
      {
        branchId: null,
        branchName: '经典传统做法',
        cuisineType: '',
        dietMode: 0,
        coverImageId: null,
        fileList: [],
        calories: 0,
        protein: 0,
        fat: 0,
        carbs: 0,
        autoCalculateNutrients: true,
        ingredients: [],
        steps: []
      }
    ]
  }
  modalVisible.value = true
}

const openEditModal = async (dishId: number, branchId: number) => {
  loading.value = true
  activeTab.value = 'basic'
  activeBranchIdx.value = 0
  try {
    const detail: any = await request.get(`/api/dish/detail?dishId=${dishId}`)
    const dish = detail.dish

    // 格式化主成品图（仅包含单个 coverImageId）
    if (dish.coverImageId) {
      fileList.value = [{
        name: `image-${dish.coverImageId}`,
        storageId: dish.coverImageId,
        url: `${BASE_URL}/api/s3Storage/preview/${dish.coverImageId}`
      }]
    } else {
      fileList.value = []
    }

    // 获取做法分支
    let branches = detail.branches
    if (!branches || branches.length === 0) {
      branches = [
        {
          branchId: null,
          branchName: '经典做法',
          cuisineType: '',
          dietMode: 0,
          calories: 0,
          protein: 0,
          fat: 0,
          carbs: 0,
          autoCalculateNutrients: true,
          ingredients: detail.ingredients || [],
          steps: detail.steps || []
        }
      ]
    }

    form.value = {
      dishId: dish.dishId,
      dishName: dish.dishName,
      cuisineType: dish.cuisineType || '', // 正确回显主菜系
      dietMode: dish.dietMode ?? 0,       // 正确回显建议膳食模式
      coverImageId: dish.coverImageId,
      branches: branches.map((b: any) => {
        // 回显每个做法分支的专属封面图片
        let bFileList: any[] = []
        if (b.coverImageId) {
          bFileList = [{
            name: `branch-image-${b.coverImageId}`,
            storageId: b.coverImageId,
            url: `${BASE_URL}/api/s3Storage/preview/${b.coverImageId}`
          }]
        }
        return {
          branchId: b.branchId,
          branchName: b.branchName,
          cuisineType: b.cuisineType || '',
          dietMode: b.dietMode || 0,
          coverImageId: b.coverImageId,
          fileList: bFileList,
          calories: b.calories,
          protein: b.protein,
          fat: b.fat,
          carbs: b.carbs,
          autoCalculateNutrients: b.autoCalculateNutrients ?? true,
          ingredients: b.ingredients ? b.ingredients.map((ing: any) => ({
            ingredientId: ing.ingredientId,
            useAmount: Number(ing.useAmount),
            mainMaterialFlag: ing.mainMaterialFlag
          })) : [],
          steps: b.steps ? b.steps.map((st: any) => ({
            stepPoolId: st.stepPoolId,
            stepNum: st.stepNum,
            customDetail: st.customDetail || st.stepDetail,
            durationSeconds: st.durationSeconds ?? 60,
            firePower: st.firePower ?? 0
          })) : []
        }
      })
    }

    // 自动高亮高频率点击的对应做法分支
    if (branchId) {
      const targetIdx = form.value.branches.findIndex((b: any) => b.branchId === branchId)
      if (targetIdx !== -1) {
        activeBranchIdx.value = targetIdx
      }
    }

    modalVisible.value = true
  } catch (e) {
    console.error('获取菜品详情失败', e)
  } finally {
    loading.value = false
  }
}

// 上传
const handleUploadSuccess = (response: any, uploadFile: any) => {
  if (response && response.code === 200) {
    uploadFile.storageId = response.data.storageId
    uploadFile.url = `${BASE_URL}/api/s3Storage/preview/${response.data.storageId}`
    form.value.coverImageId = response.data.storageId
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleRemove = () => {
  form.value.coverImageId = null
}

// 做法分支封面图上传处理器
const handleBranchUploadSuccess = (response: any, uploadFile: any, branch: any) => {
  if (response && response.code === 200) {
    uploadFile.storageId = response.data.storageId
    uploadFile.url = `${BASE_URL}/api/s3Storage/preview/${response.data.storageId}`
    branch.coverImageId = response.data.storageId
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleBranchRemove = (branch: any) => {
  branch.coverImageId = null
}

// 保存
const handleSave = async () => {
  if (!form.value.dishName.trim()) {
    ElMessage.warning('请输入菜品名称！')
    return
  }
  if (!form.value.cuisineType) {
    ElMessage.warning('请选择菜系类型！')
    return
  }
  if (form.value.branches.length === 0) {
    ElMessage.warning('请至少添加一个烹饪做法分支！')
    return
  }

  // 验证各分支用料和工序
  for (let i = 0; i < form.value.branches.length; i++) {
    const b = form.value.branches[i]
    if (!b.branchName.trim()) {
      ElMessage.warning(`第 ${i + 1} 个做法分支名称未填写！`)
      return
    }
    for (let j = 0; j < b.ingredients.length; j++) {
      const ing = b.ingredients[j]
      if (!ing.ingredientId) {
        ElMessage.warning(`分支“${b.branchName}”的第 ${j + 1} 行食材未选择！`)
        return
      }
      if (!ing.useAmount || ing.useAmount <= 0) {
        ElMessage.warning(`料量配方必须大于0！`)
        return
      }
    }
  }

  try {
    const res = await request.post('/api/dish/save', form.value)
    if (res) {
      ElMessage.success('菜谱数据保存重算成功！')
      modalVisible.value = false
      loadDishes()
    }
  } catch (e) {
    console.error('保存菜品失败', e)
  }
}

const handleDelete = (branchId: number, displayName: string) => {
  ElMessageBox.confirm(
      `确认删除做法“${displayName}”及配方？若此做法是该菜谱下的最后一个有效分支，主菜谱也将被联动删除。此操作不可逆。`,
      '安全警告',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      const res = await request.post('/api/dish/delete/branch', {id: branchId})
      if (res) {
        ElMessage.success('做法分支删除成功！')
        loadDishes()
      }
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {
  })
}

onMounted(() => {
  loadDishes()
  loadDicts()
})
</script>

<style scoped>
.panel-header-section {
  margin-bottom: 24px;
  border-bottom: 1px solid var(--hairline);
  padding-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 500;
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 6px 0;
}

.title-icon {
  font-size: 20px;
  color: var(--primary);
}

.sub-title {
  font-size: 13px;
  color: var(--ink-subtle);
  display: block;
}

.search-bar {
  margin-bottom: 20px;
}

.operation-bar {
  margin-bottom: 20px;
}

.dish-preview-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--rounded-sm);
  border: 1px solid var(--hairline);
}

.unit {
  font-size: 11px;
  color: var(--ink-subtle);
}

/* 弹出编辑器布局 */
.dish-editor-scrollable {
  max-height: 55vh;
  overflow-y: auto;
  padding-right: 12px;
}

.section-title {
  border-bottom: 1px solid var(--hairline);
  padding-bottom: 6px;
  margin-bottom: 16px;
}

.section-title h3 {
  font-size: 15px;
  font-weight: 700;
  margin-top: 4px;
}

.sub-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-row-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* 做法多分支配置面板布局 */
.branch-editor-layout {
  display: flex;
  gap: 16px;
  height: 52vh;
}

.branch-list-sidebar {
  width: 200px;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  font-size: 13px;
  font-weight: bold;
}

.sidebar-body {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-right: 6px;
}

.sidebar-empty {
  text-align: center;
  padding: 20px 0;
  color: #95a5a6;
  font-size: 11px;
}

.branch-detail-form {
  flex: 1;
  overflow-y: auto;
}

.branch-detail-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7f8c8d;
  font-size: 13px;
}

/* 动态列表配料 */
.row-col {
  flex: 1;
}

.btn-delete-row {
  background: transparent;
  border: none;
  color: #7f8c8d;
  cursor: pointer;
  font-size: 14px;
}

.btn-delete-row:hover {
  color: #e74c3c;
}

/* 步骤工序编辑 */
.steps-rows-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.step-template-select-col {
  display: flex;
  align-items: center;
}

.step-actions-col {
  display: flex;
  gap: 4px;
}

.btn-step-act {
  background-color: white;
  border: 1px solid #ebedf0;
  color: #7f8c8d;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-step-act:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.text-danger {
  color: #e74c3c !important;
}

.step-card-body {
  width: 100%;
}

/* 分页器布局 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 优质交互与设计系统精修 */
.dish-title-text {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}

.branch-tags-list {
  margin-top: var(--spacing-xs);
  display: flex;
  gap: var(--spacing-xxs);
  flex-wrap: wrap;
}

.mini-branch-tag {
  border-radius: var(--rounded-xs);
  font-weight: 500;
  font-size: 11px;
}

.animate-thumb {
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.animate-thumb:hover {
  transform: scale(1.12);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 低饱和度膳食模式微调 */
.mode-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: var(--rounded-pill);
  font-size: 11px;
  font-weight: 600;
  border: 1px solid transparent;
}

.badge-lime {
  background-color: #f0fdf4;
  color: #166534;
  border-color: #bbf7d0;
}

.badge-red {
  background-color: #fef2f2;
  color: #991b1b;
  border-color: #fecaca;
}

.badge-gray {
  background-color: #f9fafb;
  color: #374151;
  border-color: #e5e7eb;
}

/* 做法分支侧边栏选中高亮与微质感 */
.branch-sidebar-item {
  padding: 10px 14px;
  border-radius: var(--rounded-sm);
  background-color: var(--surface-2);
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.branch-sidebar-item:hover {
  background-color: var(--surface-3);
}

.branch-sidebar-item.active {
  background-color: rgba(24, 29, 38, 0.06);
  color: var(--primary);
  font-weight: 600;
  border-left: 3px solid var(--primary);
}

.branch-sidebar-item.active .b-name {
  color: var(--primary);
}

/* 精美自动核算提示盒 */
.alert-info-box {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-xs);
  padding: 12px 16px;
  background-color: var(--signature-cream);
  border: 1px solid var(--hairline-strong);
  border-radius: var(--rounded-md);
  margin: var(--spacing-xs) 0 var(--spacing-md) 0;
}

.info-icon {
  font-size: 16px;
}

.text-info-highlight {
  color: var(--ink) !important;
  line-height: 1.4;
  margin: 0;
}

/* 配料卡片化排版 */
.dynamic-row-card {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background-color: var(--surface-2);
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  padding: 12px 16px;
  margin-bottom: 10px;
}

.input-label-caption {
  font-size: 11px;
  color: var(--ink-tertiary);
  font-weight: 600;
  margin-bottom: 4px;
  display: block;
}

.input-unit-wrapper {
  display: flex;
  align-items: center;
  position: relative;
  width: 100%;
}

.input-unit-wrapper .input-text {
  padding-right: 32px;
}

.unit-tag {
  position: absolute;
  right: 12px;
  font-size: 11px;
  color: var(--ink-subtle);
  pointer-events: none;
}

.card-empty-style {
  background-color: var(--surface-2);
  border: 1px dashed var(--hairline-strong);
  border-radius: var(--rounded-md);
  padding: var(--spacing-lg) 0;
  text-align: center;
  color: var(--ink-subtle);
  font-size: 12px;
}

/* 步骤工序精细编辑器 */
.step-card-editor {
  border-radius: var(--rounded-md);
  background-color: #fcfdfe;
  border: 1px solid #e5e9f2 !important;
  padding: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.01);
  transition: box-shadow 0.15s ease;
}

.step-card-editor:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
}

.step-num-badge {
  background-color: rgba(24, 29, 38, 0.08);
  padding: 3px 8px;
  border-radius: var(--rounded-xs);
}

.step-num {
  font-size: 11px;
  font-weight: 700;
  color: var(--primary);
}

.textarea-editor {
  width: 100%;
  background-color: var(--canvas);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: 13px;
  border-radius: var(--rounded-sm);
  padding: 8px 12px;
  border: 1px solid var(--hairline);
  outline: none;
  resize: vertical;
  line-height: 1.5;
  transition: all 0.15s ease;
}

.textarea-editor:focus {
  border-color: var(--info-border);
  box-shadow: 0 0 0 2px rgba(69, 143, 255, 0.2);
}

/* ==========================================
   做法配方右侧抽屉 Drawer 专有美学样式
   ========================================== */
.drawer-detail-content {
  display: flex;
  flex-direction: column;
  padding: var(--spacing-sm);
}

.metrics-summary-card .metric-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.metrics-summary-card .metric-block:not(:last-child) {
  border-right: 1px dashed var(--hairline);
}

.metrics-summary-card .label {
  font-size: 11px;
  color: var(--ink-tertiary);
  margin-bottom: 4px;
}

.metrics-summary-card .val {
  font-size: 18px;
  font-weight: 700;
  color: var(--ink);
}

.metrics-summary-card .val .unit {
  font-size: 11px;
  font-weight: 400;
  color: var(--ink-subtle);
}

.detail-section .section-sub-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: 12px;
  border-left: 3px solid var(--primary);
  padding-left: 8px;
}

.materials-list-view {
  border: 1px solid var(--hairline);
  border-radius: var(--rounded-md);
  overflow: hidden;
  background-color: var(--surface-1);
}

.material-item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  transition: background-color 0.15s ease;
}

.material-item-row:not(:last-child) {
  border-bottom: 1px dashed var(--hairline-tertiary);
}

.material-item-row:hover {
  background-color: var(--surface-2);
}

.material-item-row .m-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
}

.material-item-row .m-amount {
  font-size: 13px;
  color: var(--ink-subtle);
}

/* 时间线步骤排版 */
.steps-list-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-left: 4px;
}

.step-item-view {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  position: relative;
}

.step-item-view:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 28px;
  top: 30px;
  bottom: -22px;
  width: 1px;
  border-left: 1px dashed var(--hairline-strong);
}

.step-item-view .step-header {
  flex-shrink: 0;
}

.step-item-view .step-num {
  display: inline-block;
  background-color: rgba(24, 29, 38, 0.08);
  color: var(--primary);
  padding: 4px 10px;
  border-radius: var(--rounded-sm);
  font-weight: 700;
  font-size: 11px;
}

.step-item-view .step-desc {
  flex-grow: 1;
  color: var(--ink-muted);
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
  padding-top: 2px;
}

.step-item-view .step-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.step-item-view .step-meta-info {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 2px;
}

.step-item-view .meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  line-height: 1.2;
}

.step-item-view .meta-tag.fire {
  background-color: rgba(249, 115, 22, 0.08);
  color: #ea580c;
  border: 1px solid rgba(249, 115, 22, 0.15);
}

.step-item-view .meta-tag.duration {
  background-color: rgba(59, 130, 246, 0.08);
  color: #2563eb;
  border: 1px solid rgba(59, 130, 246, 0.15);
}

.step-item-view .meta-icon {
  font-size: 11px;
}

.empty-tip-small {
  text-align: center;
  padding: 24px 0;
  color: var(--ink-tertiary);
  font-size: 12px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: var(--spacing-md);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--hairline);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
