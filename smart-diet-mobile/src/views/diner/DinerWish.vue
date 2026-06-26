<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {useRoleStore} from '../../store/role'
import request from '../../utils/request'
import {showSuccessToast, showToast} from 'vant'

const roleStore = useRoleStore()
const loading = ref(false)

// 菜谱词典（用于提供给用户搜索和选择）
const mockDishes = [
  {dishId: 1, name: '红烧肉', type: '放纵大餐'},
  {dishId: 2, name: '水煮鱼', type: '正常饮食'},
  {dishId: 3, name: '西红柿炒鸡蛋', type: '正常饮食'},
  {dishId: 4, name: '清蒸鲈鱼', type: '轻食减脂'},
  {dishId: 5, name: '香煎鸡胸肉', type: '轻食减脂'},
  {dishId: 6, name: '麻婆豆腐', type: '正常饮食'},
  {dishId: 7, name: '糖醋排骨', type: '放纵大餐'},
  {dishId: 8, name: '白灼西兰花', type: '轻食减脂'}
]

// 绑定的心愿列表与不喜欢列表
const wishList = ref<any[]>([])
const dislikeList = ref<any[]>([])

// 心愿表单
const showWishDialog = ref(false)
const wishForm = ref({
  dishId: null as number | null,
  dishName: '',
  wishDate: new Date().toISOString().split('T')[0],
  note: ''
})

// 忌口表单
const showDislikeDialog = ref(false)
const dislikeForm = ref({
  dishId: null as number | null,
  dishName: ''
})

// 搜索选择菜品相关
const showDishSearch = ref(false)
const searchKey = ref('')
const searchTarget = ref<'wish' | 'dislike'>('wish')
const filteredDishes = ref<any[]>(mockDishes)

const onSearchInput = () => {
  if (!searchKey.value) {
    filteredDishes.value = mockDishes
  } else {
    filteredDishes.value = mockDishes.filter(d => d.name.includes(searchKey.value))
  }
}

const openDishSelect = (target: 'wish' | 'dislike') => {
  searchTarget.value = target
  searchKey.value = ''
  filteredDishes.value = mockDishes
  showDishSearch.value = true
}

const selectDish = (dish: any) => {
  if (searchTarget.value === 'wish') {
    wishForm.value.dishId = dish.dishId
    wishForm.value.dishName = dish.name
  } else {
    dislikeForm.value.dishId = dish.dishId
    dislikeForm.value.dishName = dish.name
  }
  showDishSearch.value = false
}

// 模拟加载及提交接口
const fetchLists = async () => {
  if (roleStore.token?.startsWith('mock-')) {
    // 模拟数据展示
    wishList.value = [
      {id: 201, dishName: '红烧肉', wishDate: '2026-06-27', note: '周末想解解馋，吃一次放纵餐！'},
      {id: 202, dishName: '水煮鱼', wishDate: '2026-06-29', note: '想吃麻辣口的'}
    ]
    dislikeList.value = [
      {id: 301, dishName: '香菜拌牛肉', count: 1},
      {id: 302, dishName: '洋葱炒肉', count: 2}
    ]
    return
  }

  loading.value = true
  try {
    const wishesRes = await request.get(`/api/diet/wish-dish/list?profileId=${roleStore.profileId}`)
    wishList.value = wishesRes.data || wishesRes || []

    const dislikeRes = await request.get(`/api/diet/dislike-dish/list?profileId=${roleStore.profileId}`)
    dislikeList.value = dislikeRes.data || dislikeRes || []
  } catch (err) {
    console.error('拉取心愿/忌口列表失败，使用本地备用数据', err)
  } finally {
    loading.value = false
  }
}

const submitWish = async () => {
  if (!wishForm.value.dishId) {
    showToast('请选择菜品')
    return
  }

  try {
    if (roleStore.token?.startsWith('mock-')) {
      wishList.value.unshift({
        id: Date.now(),
        dishName: wishForm.value.dishName,
        wishDate: wishForm.value.wishDate,
        note: wishForm.value.note
      })
      showSuccessToast('心愿提交成功')
    } else {
      await request.post('/api/diet/wish-dish/add', {
        profileId: roleStore.profileId,
        groupId: roleStore.groupId,
        dishId: wishForm.value.dishId,
        wishDate: wishForm.value.wishDate,
        note: wishForm.value.note
      })
      showSuccessToast('心愿提交成功')
      fetchLists()
    }
    showWishDialog.value = false
    wishForm.value = {dishId: null, dishName: '', wishDate: new Date().toISOString().split('T')[0], note: ''}
  } catch (err) {
    console.error(err)
  }
}

const submitDislike = async () => {
  if (!dislikeForm.value.dishId) {
    showToast('请选择菜品')
    return
  }

  try {
    if (roleStore.token?.startsWith('mock-')) {
      const existing = dislikeList.value.find(d => d.dishName === dislikeForm.value.dishName)
      if (existing) {
        existing.count++
      } else {
        dislikeList.value.unshift({
          id: Date.now(),
          dishName: dislikeForm.value.dishName,
          count: 1
        })
      }
      showSuccessToast('不爱吃偏好已记录')
    } else {
      await request.post('/api/diet/dislike-dish/add', {
        profileId: roleStore.profileId,
        groupId: roleStore.groupId,
        dishId: dislikeForm.value.dishId
      })
      showSuccessToast('已申报不喜欢菜品')
      fetchLists()
    }
    showDislikeDialog.value = false
    dislikeForm.value = {dishId: null, dishName: ''}
  } catch (err) {
    console.error(err)
  }
}

const deleteWish = async (id: number) => {
  try {
    if (roleStore.token?.startsWith('mock-')) {
      wishList.value = wishList.value.filter(item => item.id !== id)
      showSuccessToast('已撤销心愿')
    } else {
      await request.delete(`/api/diet/wish-dish/${id}`)
      showSuccessToast('已撤销心愿')
      fetchLists()
    }
  } catch (err) {
    console.error(err)
  }
}

const deleteDislike = async (id: number) => {
  try {
    if (roleStore.token?.startsWith('mock-')) {
      dislikeList.value = dislikeList.value.filter(item => item.id !== id)
      showSuccessToast('已移除记录')
    } else {
      await request.delete(`/api/diet/dislike-dish/${id}`)
      showSuccessToast('已移出黑名单')
      fetchLists()
    }
  } catch (err) {
    console.error(err)
  }
}

onMounted(() => {
  fetchLists()
})
</script>

<template>
  <div class="diner-wish">
    <!-- 想吃心愿单模块 -->
    <div class="section-card card-shadow">
      <div class="card-title-bar">
        <h3>🍲 心愿单 (我想吃)</h3>
        <van-button icon="plus" type="primary" size="small" round @click="showWishDialog = true"/>
      </div>

      <div class="list-container">
        <p class="empty-tip" v-if="wishList.length === 0">还没有提交想吃的菜，点 + 添加吧</p>

        <van-swipe-cell v-for="item in wishList" :key="item.id" class="list-item-swipe">
          <div class="list-item">
            <div class="item-left">
              <span class="dish-title">{{ item.dishName }}</span>
              <span class="wish-date">期望就餐日期: {{ item.wishDate }}</span>
              <p class="wish-note" v-if="item.note">“{{ item.note }}”</p>
            </div>
            <div class="tag-status">已提交</div>
          </div>

          <template #right>
            <van-button square type="danger" text="撤销" class="delete-button" @click="deleteWish(item.id)"/>
          </template>
        </van-swipe-cell>
      </div>
    </div>

    <!-- 不爱吃避坑偏好模块 -->
    <div class="section-card card-shadow mt-16">
      <div class="card-title-bar">
        <h3>🚫 避坑单 (不爱吃)</h3>
        <van-button icon="plus" type="warning" size="small" round @click="showDislikeDialog = true"/>
      </div>

      <div class="list-container">
        <p class="empty-tip" v-if="dislikeList.length === 0">没有不爱吃的菜，给您点赞！</p>

        <van-swipe-cell v-for="item in dislikeList" :key="item.id" class="list-item-swipe">
          <div class="list-item">
            <div class="item-left">
              <span class="dish-title">{{ item.dishName }}</span>
              <span class="dislike-count">已吐槽次数: {{ item.count }} 次（累计3次彻底不予推荐）</span>
            </div>
            <van-tag type="warning" round v-if="item.count >= 3">彻底屏蔽</van-tag>
            <van-tag type="default" round v-else>避忌中</van-tag>
          </div>

          <template #right>
            <van-button square type="danger" text="移出" class="delete-button" @click="deleteDislike(item.id)"/>
          </template>
        </van-swipe-cell>
      </div>
    </div>

    <!-- 弹窗：添加心愿 -->
    <van-dialog
        v-model:show="showWishDialog"
        title="我想吃点什么"
        show-cancel-button
        @confirm="submitWish"
    >
      <div class="dialog-form">
        <van-field
            v-model="wishForm.dishName"
            is-link
            readonly
            label="菜品"
            placeholder="点击选择想吃的菜品"
            @click="openDishSelect('wish')"
        />
        <van-field
            v-model="wishForm.wishDate"
            type="date"
            label="期望日期"
            placeholder="选择期望日期"
        />
        <van-field
            v-model="wishForm.note"
            rows="2"
            autosize
            label="留言备注"
            type="textarea"
            maxlength="50"
            placeholder="可以写微辣、少盐或者期望的做法..."
            show-word-limit
        />
      </div>
    </van-dialog>

    <!-- 弹窗：添加不爱吃 -->
    <van-dialog
        v-model:show="showDislikeDialog"
        title="申报不喜欢的菜"
        show-cancel-button
        @confirm="submitDislike"
    >
      <div class="dialog-form">
        <van-field
            v-model="dislikeForm.dishName"
            is-link
            readonly
            label="菜品"
            placeholder="点击选择不喜欢的菜品"
            @click="openDishSelect('dislike')"
        />
      </div>
    </van-dialog>

    <!-- 侧边弹窗：菜品搜索选择 -->
    <van-popup
        v-model:show="showDishSearch"
        position="right"
        :style="{ width: '80%', height: '100%' }"
    >
      <div class="search-panel">
        <van-search
            v-model="searchKey"
            placeholder="搜索菜品库"
            @update:model-value="onSearchInput"
        />
        <div class="search-results">
          <van-cell
              v-for="dish in filteredDishes"
              :key="dish.dishId"
              :title="dish.name"
              :label="dish.type"
              is-link
              @click="selectDish(dish)"
          />
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.diner-wish {
  padding-bottom: 24px;
}

.section-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
}

.mt-16 {
  margin-top: 16px;
}

.card-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 8px;
}

.card-title-bar h3 {
  font-size: 15px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.list-container {
  display: flex;
  flex-direction: column;
}

.empty-tip {
  font-size: 12px;
  color: var(--text-color-secondary);
  text-align: center;
  padding: 30px 0;
}

.list-item-swipe {
  border-bottom: 1px solid #f7f8fa;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 4px;
  background: white;
}

.item-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: left;
}

.dish-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color-primary);
}

.wish-date {
  font-size: 11px;
  color: var(--text-color-secondary);
}

.wish-note {
  font-size: 12px;
  color: #e67e22;
  margin-top: 2px;
  font-style: italic;
}

.dislike-count {
  font-size: 10px;
  color: #c0392b;
}

.tag-status {
  font-size: 11px;
  color: var(--primary-color);
  font-weight: 500;
}

.delete-button {
  height: 100%;
}

.dialog-form {
  padding: 16px 8px;
}

.search-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.search-results {
  flex: 1;
  overflow-y: auto;
}
</style>
