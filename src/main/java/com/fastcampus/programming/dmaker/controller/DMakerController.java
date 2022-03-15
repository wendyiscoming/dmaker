package com.fastcampus.programming.dmaker.controller;

import com.fastcampus.programming.dmaker.dto.*;
import com.fastcampus.programming.dmaker.exception.DMakerException;
import com.fastcampus.programming.dmaker.service.DmakerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DMakerController {
    private final DmakerService dmakerService;

    @GetMapping("/developers")
    public List<DeveloperDto> getAllDevelopers() {
        log.info("GET /developers HTTP/1.1");

        return dmakerService.getAllEmployedDevelopers();
    }

    @GetMapping("/developer/{memberId}")
    public DeveloperDetailDto getDeveloperDetail(@PathVariable final String memberId) {
        log.info("GET /developers HTTP/1.1");

        return dmakerService.getDeveloperDetail(memberId);
    }

    @PostMapping("/create-developer")
    public CreateDeveloper.Response createDevelopers(@Valid @RequestBody final CreateDeveloper.Request request) {
        log.info("request: {}", request);

        return dmakerService.createDeveloper(request);
    }

    @PutMapping("/developer/{memberId}")
    public DeveloperDetailDto editDeveloper(@PathVariable final String memberId,
                                            @Valid @RequestBody final EditDeveloper.Request request) {
        log.info("GET /developers HTTP/1.1");

        return dmakerService.editDeveloper(memberId, request);
    }

    @DeleteMapping("/developer/{memberId}")
    public DeveloperDetailDto deleteDeveloper(@PathVariable final String memberId) {
        return dmakerService.deleteDeveloper(memberId);
    }
}
