package com.seven.auth.role;

import com.seven.auth.Pagination;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.util.response.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.seven.auth.util.response.Responder.noContent;
import static com.seven.auth.util.response.Responder.ok;

@RestController
@RequestMapping("/role")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "roleId") UUID id)  throws AuthorizationException {
        RoleDTO roleDTO = roleService.get(id);
        return ok(roleDTO);
    }

    @GetMapping
    public ResponseEntity <Response> getResources(Pagination pagination, RoleRequest.Filter roleFilter)  throws AuthorizationException {
        Page<RoleDTO> roleDTOs = roleService.getAll(pagination, roleFilter);
        return ok(roleDTOs);
    }

    @PostMapping
    public ResponseEntity <Response> createResource(@Valid @RequestBody RoleRequest.Create request) throws AuthorizationException {
        RoleDTO roleDTO = roleService.create(request);
        return ok(roleDTO);
    }

    @DeleteMapping("{roleId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "roleId") UUID id) throws AuthorizationException {
        roleService.delete(id);
        return noContent();
    }
}