package com.seven.auth.grant;

import com.seven.auth.util.Constants;
import com.seven.auth.util.Pagination;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.util.response.Response;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.seven.auth.util.response.Responder.noContent;
import static com.seven.auth.util.response.Responder.ok;

@RestController
@RequestMapping("/grants")
public class GrantController {
    private final GrantService grantService;
    public GrantController(GrantService grantService) {
        this.grantService = grantService;
    }

    @GetMapping("{grantId}")
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "grantId") UUID id)  throws AuthorizationException {
        GrantDTO.Record grantRecord = grantService.get(id);
        return ok(grantRecord);
    }

    @GetMapping
    public ResponseEntity <Response> getResources(@ParameterObject Pagination pagination,  @ParameterObject GrantDTO.Filter grantFilter)  throws AuthorizationException {
        Page<GrantDTO.Record> grantRecords = grantService.getAll(pagination, grantFilter);
        return ok(grantRecords);
    }

    @PostMapping
    public ResponseEntity <Response> createResource(@Valid @RequestBody GrantDTO.Create request) throws AuthorizationException {
        GrantDTO.Record grantRecord = grantService.create(request);
        return ok(grantRecord);
    }

//    @PutMapping("/{grantId}")
//    public ResponseEntity <Response> updateResource(@Valid @NotNull @PathVariable(value = "grantId") UUID id, @Valid @RequestBody GrantDTO.Update request) {
//       GrantDTO.Record grantRecord = grantService.update(id, request);
//        return ok(grantRecord);
//    }
    
    @DeleteMapping("{grantId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "grantId") UUID id) throws AuthorizationException {
        grantService.delete(id);
        return noContent();
    }
}