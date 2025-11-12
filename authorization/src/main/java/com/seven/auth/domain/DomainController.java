package com.seven.auth.domain;

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
@RequestMapping("/domain")
public class DomainController {
    private final DomainService domainService;
    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    @GetMapping
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "domainId") UUID id)  throws AuthorizationException {
        DomainDTO.Record domainRecord = domainService.get(id);
        return ok(domainRecord);
    }

    @GetMapping
    public ResponseEntity <Response> getResources(Pagination pagination, DomainDTO.Filter domainFilter)  throws AuthorizationException {
        Page<DomainDTO.Record> domainRecords = domainService.getAll(pagination, domainFilter);
        return ok(domainRecords);
    }

    @PostMapping
    public ResponseEntity <Response> createResource(@Valid @RequestBody DomainDTO.Create request) throws AuthorizationException {
        DomainDTO.Record domainRecord = domainService.create(request);
        return ok(domainRecord);
    }

//    @PutMapping("/{domainId}")
//    public ResponseEntity <Response> updateResource(@Valid @NotNull @PathVariable(value = "domainId") UUID id, @Valid @RequestBody DomainDTO.Update request) {
//        DomainDTO.Record domainRecord = domainService.update(id, request);
//        return ok(domainRecord);
//    }
    
    @DeleteMapping("{domainId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "domainId") UUID id) throws AuthorizationException {
        domainService.delete(id);
        return noContent();
    }
}