package com.seven.auth.permission;

public enum PEnum {
    super_create, super_read, super_update, super_delete,
    elev_create, elev_read, elev_update, elev_delete,
    create_domain, read_domain, update_domain, delete_domain,
    create_permission, read_permission, update_permission, delete_permission,
    create_role, read_role, update_role, delete_role,
    create_assignment, read_assignment, update_assignment, delete_assignment,
    create_grant, read_grant, update_grant, delete_grant,
    create_account, read_account, update_account, delete_account
}
