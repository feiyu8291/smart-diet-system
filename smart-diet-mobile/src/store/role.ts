import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useRoleStore = defineStore('role', () => {
    // 从本地缓存初始化状态，方便刷新页面后状态不丢失
    const role = ref<'chef' | 'diner' | null>(localStorage.getItem('role') as any)
    const token = ref<string | null>(localStorage.getItem('token'))
    const userId = ref<string | null>(localStorage.getItem('userId'))
    const userName = ref<string | null>(localStorage.getItem('userName'))
    const groupId = ref<number>(Number(localStorage.getItem('groupId') || 0))
    const profileId = ref<number>(Number(localStorage.getItem('profileId') || 0))

    // 用户所拥有的角色列表和家庭组列表
    const userRoles = ref<('chef' | 'diner')[]>(JSON.parse(localStorage.getItem('userRoles') || '["chef", "diner"]'))
    const userGroups = ref<{ id: number; name: string }[]>(
        JSON.parse(localStorage.getItem('userGroups') || '[{"id": 1, "name": "健康快乐一家人"}, {"id": 2, "name": "活力四射健身组"}]')
    )

    // 保存登录认证状态
    function setAuth(data: { token: string; userId: string; userName: string }) {
        token.value = data.token
        userId.value = data.userId
        userName.value = data.userName
        localStorage.setItem('token', data.token)
        localStorage.setItem('userId', data.userId)
        localStorage.setItem('userName', data.userName)
    }

    // 切换/设定当前角色
    function setRole(newRole: 'chef' | 'diner') {
        role.value = newRole
        localStorage.setItem('role', newRole)
    }

    // 绑定家庭组与就餐人档案
    function setGroupAndProfile(gId: number, pId: number = 0) {
        groupId.value = gId
        profileId.value = pId
        localStorage.setItem('groupId', String(gId))
        localStorage.setItem('profileId', String(pId))
    }

    // 设定该用户拥有的角色列表和家庭组列表
    function setRolesAndGroups(roles: ('chef' | 'diner')[], groups: { id: number; name: string }[]) {
        userRoles.value = roles
        userGroups.value = groups
        localStorage.setItem('userRoles', JSON.stringify(roles))
        localStorage.setItem('userGroups', JSON.stringify(groups))
    }

    // 切换当前角色（视角一键穿梭）
    function switchRole() {
        if (userRoles.value.includes('chef') && userRoles.value.includes('diner')) {
            const nextRole = role.value === 'chef' ? 'diner' : 'chef'
            setRole(nextRole)
            return nextRole
        }
        return role.value
    }

    // 切换当前选择的家庭组
    function changeGroup(gId: number) {
        groupId.value = gId
        localStorage.setItem('groupId', String(gId))
        // 自动重置/切换 profileId：这里可以根据家庭组来决定档案，若是演示也可以保留现有
    }

    // 清除状态（退出登录）
    function clear() {
        role.value = null
        token.value = null
        userId.value = null
        userName.value = null
        groupId.value = 0
        profileId.value = 0
        userRoles.value = ['chef', 'diner']
        userGroups.value = [{id: 1, name: '健康快乐一家人'}, {id: 2, name: '活力四射健身组'}]
        localStorage.removeItem('role')
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('userName')
        localStorage.removeItem('groupId')
        localStorage.removeItem('profileId')
        localStorage.removeItem('userRoles')
        localStorage.removeItem('userGroups')
    }

    return {
        role,
        token,
        userId,
        userName,
        groupId,
        profileId,
        userRoles,
        userGroups,
        setAuth,
        setRole,
        setGroupAndProfile,
        setRolesAndGroups,
        switchRole,
        changeGroup,
        clear
    }
})
