package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.PageUtils;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.mapper.PermissionMapper;
import com.zhhs.nong.mapper.RoleMapper;
import com.zhhs.nong.model.Permission;
import com.zhhs.nong.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AccessControlService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final OperationLogService logService;

    public AccessControlService(RoleMapper roleMapper,
                                PermissionMapper permissionMapper,
                                OperationLogService logService) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRoles(Integer page, Integer pageSize) {
        List<Role> all = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .orderByDesc(Role::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPermissions(Integer page, Integer pageSize) {
        List<Permission> all = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .orderByDesc(Permission::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional
    public Role updateRoleMembers(Long id, Integer members, String operator) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(4046, "role not found");
        }
        role.setMembers(members);
        roleMapper.updateById(role);
        logService.log(operator, "update_role_members", "角色 " + role.getRole() + " 成员数更新为 " + members);
        return role;
    }

    @Transactional
    public Role createRole(String roleName, String description, String operator) {
        Role r = new Role();
        r.setRole(roleName);
        r.setDescription(description);
        r.setMembers(0);
        roleMapper.insert(r);
        logService.log(operator, "create_role", "新增角色 " + roleName);
        return r;
    }

    @Transactional
    public Role updateRole(Long id, String roleName, String description, String operator) {
        Role r = roleMapper.selectById(id);
        if (r == null) {
            throw new BizException(4046, "role not found");
        }
        if (roleName != null) r.setRole(roleName);
        if (description != null) r.setDescription(description);
        roleMapper.updateById(r);
        logService.log(operator, "update_role", "编辑角色 " + r.getRole());
        return r;
    }

    @Transactional
    public void deleteRole(Long id, String operator) {
        Role r = roleMapper.selectById(id);
        if (r == null) {
            throw new BizException(4046, "role not found");
        }
        roleMapper.deleteById(id);
        logService.log(operator, "delete_role", "删除角色 " + r.getRole());
    }

    @Transactional
    public Permission createPermission(String module, String action, String role, String operator) {
        Permission permission = new Permission();
        permission.setModule(module);
        permission.setAction(action);
        permission.setRole(role);
        permissionMapper.insert(permission);
        logService.log(operator, "create_permission", "新增权限 " + module + "/" + action + " -> " + role);
        return permission;
    }

    @Transactional
    public Permission updatePermission(Long id, String module, String action, String role, String operator) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BizException(4050, "permission not found");
        }
        permission.setModule(module);
        permission.setAction(action);
        permission.setRole(role);
        permissionMapper.updateById(permission);
        logService.log(operator, "update_permission", "编辑权限 " + id);
        return permission;
    }

    @Transactional
    public void deletePermission(Long id, String operator) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BizException(4050, "permission not found");
        }
        permissionMapper.deleteById(id);
        logService.log(operator, "delete_permission", "删除权限 " + id);
    }
}
