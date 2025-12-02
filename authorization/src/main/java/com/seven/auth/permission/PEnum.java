package com.seven.auth.permission;

public enum PEnum {
    super_create, super_read, super_update, super_delete,
    elev_create, elev_read, elev_update, elev_delete,
    create_domain, read_domain, update_domain, delete_domain,
    create_perm, read_perm, update_perm, delete_perm,
    create_role, read_role, update_role, delete_role,
    create_asg, read_asg, update_asg, delete_asg,
    create_grant, read_grant, update_grant, delete_grant,
    create_acc, read_acc, update_acc, delete_acc
}
