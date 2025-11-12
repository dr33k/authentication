package com.seven.auth.permission;

import com.seven.auth.util.Pagination;
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
@RequestMapping("/permission")
public class PermissionController {
    private final PermissionService permissionService;
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "permissionId") UUID id)  throws AuthorizationException {
        PermissionDTO.Record permissionRecord = permissionService.get(id);
        return ok(permissionRecord);
    }

    @GetMapping
    public ResponseEntity <Response> getResources(Pagination pagination, PermissionDTO.Filter permissionFilter)  throws AuthorizationException {
        Page<PermissionDTO.Record> permissionRecords = permissionService.getAll(pagination, permissionFilter);
        return ok(permissionRecords);
    }

    @PostMapping
    public ResponseEntity <Response> createResource(@Valid @RequestBody PermissionDTO.Create request) throws AuthorizationException {
        PermissionDTO.Record permissionRecord = permissionService.create(request);
        return ok(permissionRecord);
    }

//    @PutMapping("/{permissionId}")
//    public ResponseEntity <Response> updateResource(@Valid @NotNull @PathVariable(value = "permissionId") UUID id, @Valid @RequestBody PermissionDTO.Update request) {
//       PermissionDTO.Record permissionRecord = permissionService.update(id, request);
//        return ok(permissionRecord);
//    }
    
    @DeleteMapping("{permissionId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "permissionId") UUID id) throws AuthorizationException {
        permissionService.delete(id);
        return noContent();
    }
}