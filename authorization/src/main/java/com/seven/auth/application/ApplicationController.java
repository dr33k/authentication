package com.seven.auth.application;

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
@RequestMapping("/application")
public class ApplicationController {
    private final ApplicationService applicationService;
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "applicationId") UUID id)  throws AuthorizationException {
        ApplicationDTO applicationDTO = applicationService.get(id);
        return ok(applicationDTO);
    }

    @GetMapping
    public ResponseEntity <Response> getResources(Pagination pagination, ApplicationRequest.Filter applicationFilter)  throws AuthorizationException {
        Page<ApplicationDTO> applicationDTOs = applicationService.getAll(pagination, applicationFilter);
        return ok(applicationDTOs);
    }

    @PostMapping
    public ResponseEntity <Response> createResource(@Valid @RequestBody ApplicationRequest.Create request) throws AuthorizationException {
        ApplicationDTO applicationDTO = applicationService.create(request);
        return ok(applicationDTO);
    }
    
    @DeleteMapping("{applicationId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "applicationId") UUID id) throws AuthorizationException {
        applicationService.delete(id);
        return noContent();
    }
}