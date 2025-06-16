package com.seven.auth.domain;

import com.seven.auth.Pagination;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.response.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.seven.auth.response.Responder.noContent;
import static com.seven.auth.response.Responder.ok;

@RestController
@RequestMapping("/domain")
public class DomainController {
    private final DomainService domainService;
    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    @GetMapping
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "domainId") UUID id)  throws AuthorizationException {
        DomainDTO domainDTO = domainService.get(id);
        return ok(domainDTO);
    }

    @GetMapping
    public ResponseEntity <Response> getResources(Pagination pagination, DomainRequest.Filter domainFilter)  throws AuthorizationException {
        Page<DomainDTO> domainDTOs = domainService.getAll(pagination, domainFilter);
        return ok(domainDTOs);
    }

    @PostMapping
    public ResponseEntity <Response> createResource(@Valid @RequestBody DomainRequest.Create request) throws AuthorizationException {
        DomainDTO domainDTO = domainService.create(request);
        return ok(domainDTO);
    }

    @PutMapping("/{domainId}")
    public ResponseEntity <Response> updateResource(@Valid @NotNull @PathVariable(value = "domainId") UUID id, @Valid @RequestBody DomainRequest.Update request) {
        DomainDTO domainDTO = domainService.update(id, request);
        return ok(domainDTO);
    }
    
    @DeleteMapping("{domainId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "domainId") UUID id) throws AuthorizationException {
        domainService.delete(id);
        return noContent();
    }
}