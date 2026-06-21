import request from '../utils/request';

// ==================== 用户管理 ====================
export function getUserPage(params: any) {
    return request({
        url: '/sys/user/page',
        method: 'get',
        params
    });
}

export function saveUser(data: any) {
    return request({
        url: '/sys/user/save',
        method: 'post',
        data
    });
}

export function updateUser(data: any) {
    return request({
        url: '/sys/user/update',
        method: 'post',
        data
    });
}

export function deleteUser(data: { ids: number[] }) {
    return request({
        url: '/sys/user/delete',
        method: 'post',
        data
    });
}

export function updateUserStatus(userId: number, useStatus: number) {
    return request({
        url: '/sys/user/updateStatus',
        method: 'post',
        params: {userId, useStatus}
    });
}

export function resetUserPassword(userId: number) {
    return request({
        url: '/sys/user/resetPassword',
        method: 'post',
        params: {userId}
    });
}

export function getAllRoles() {
    return request({
        url: '/sys/user/getAllRoles',
        method: 'get'
    });
}

// ==================== 角色管理 ====================
export function getRolePage(params: any) {
    return request({
        url: '/sys/role/page',
        method: 'get',
        params
    });
}

export function saveRole(data: any) {
    return request({
        url: '/sys/role/save',
        method: 'post',
        data
    });
}

export function updateRole(data: any) {
    return request({
        url: '/sys/role/update',
        method: 'post',
        data
    });
}

export function deleteRole(data: { ids: number[] }) {
    return request({
        url: '/sys/role/delete',
        method: 'post',
        data
    });
}

export function getRoleMenuIds(roleId: number) {
    return request({
        url: '/sys/role/menuIds',
        method: 'get',
        params: {roleId}
    });
}

export function configRoleMenus(data: { roleId: number, menuIds: number[] }) {
    return request({
        url: '/sys/role/configMenus',
        method: 'post',
        data
    });
}

// ==================== 菜单管理 ====================
export function getMenuTree() {
    return request({
        url: '/sys/menu/tree',
        method: 'get'
    });
}

export function saveMenu(data: any) {
    return request({
        url: '/sys/menu/save',
        method: 'post',
        data
    });
}

export function updateMenu(data: any) {
    return request({
        url: '/sys/menu/update',
        method: 'post',
        data
    });
}

export function deleteMenu(data: { ids: number[] }) {
    return request({
        url: '/sys/menu/delete',
        method: 'post',
        data
    });
}

// ==================== 字典管理 ====================
export function getDictPage(params: any) {
    return request({
        url: '/sys/dict/page',
        method: 'get',
        params
    });
}

export function getDataTypeAll() {
    return request({
        url: '/sys/dict/dataTypeAll',
        method: 'get'
    });
}

export function getFirstLevelDicts() {
    return request({
        url: '/sys/dict/firstLevel',
        method: 'get'
    });
}

export function getDictChildren(params: { parentType: string, parentCode: string }) {
    return request({
        url: '/sys/dict/children',
        method: 'get',
        params
    });
}

export function saveDict(data: any) {
    return request({
        url: '/sys/dict/save',
        method: 'post',
        data
    });
}

export function updateDict(data: any) {
    return request({
        url: '/sys/dict/update',
        method: 'post',
        data
    });
}

export function deleteDict(data: { ids: number[] }) {
    return request({
        url: '/sys/dict/delete',
        method: 'post',
        data
    });
}

export function refreshDictCache() {
    return request({
        url: '/sys/dict/refreshCache',
        method: 'post'
    });
}

// ==================== 登录日志 ====================
export function getLoginLogPage(params: any) {
    return request({
        url: '/sys/log/login/page',
        method: 'get',
        params
    });
}

// ==================== 操作日志 ====================
export function getOperationLogPage(params: any) {
    return request({
        url: '/sys/log/operation/page',
        method: 'get',
        params
    });
}

// ==================== 文件存储 ====================
export function getFileStoragePage(params: any) {
    return request({
        url: '/api/s3Storage',
        method: 'get',
        params
    });
}

export function deleteStorageFiles(data: Array<{ storageId: number, bucketName: string }>) {
    return request({
        url: '/api/s3Storage/delete',
        method: 'post',
        data
    });
}

// ==================== 鉴权相关 ====================
export function getPublicKey() {
    return request({
        url: '/api/auth/public-key',
        method: 'get'
    });
}

export function getDictMore(dataTypes: string) {
    return request({
        url: '/sys/dict/more',
        method: 'get',
        params: {dataTypes}
    });
}


