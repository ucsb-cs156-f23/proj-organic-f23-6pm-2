package edu.ucsb.cs156.organic.controllers;

import edu.ucsb.cs156.organic.entities.School;
import edu.ucsb.cs156.organic.errors.EntityNotFoundException;
import edu.ucsb.cs156.organic.repositories.SchoolRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "Schools")
@RequestMapping("/api/schools")
@RestController
@Slf4j
public class SchoolsController extends ApiController {

    @Autowired
    SchoolRepository schoolRepository;

    @Autowired
    ObjectMapper mapper;

    @Operation(summary = "List all schools")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public Iterable<School> schools() {
        log.info("REACHED HERE!!!");
        Iterable<School> schools = schoolRepository.findAll();
        log.info("schools={}", schools);
        return schools;
    }

    @Operation(summary = "Get a single school")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("")
    public School getByAbbrev(
        @Parameter(name = "abbrev") @RequestParam String abbrev) {
        School school = schoolRepository.findByAbbrev(abbrev)
                .orElseThrow(() -> new EntityNotFoundException(School.class, abbrev));

        return school;
    }

    @Operation(summary = "Create a new school")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public School postSchool(
            @Parameter(name = "abbrev", description ="school abbrevation, e.g. ucsb" ) @RequestParam String abbrev,
            @Parameter(name = "name", description ="school name e.g. UC Santa Barbara" ) @RequestParam String name,
            @Parameter(name = "termRegex", description = "term regex, e.g. [WSMF]\\d\\d") @RequestParam String termRegex,
            @Parameter(name = "termDescription", description = "Enter quarter, e.g. F23, W24, S24, M24") @RequestParam String termDescription,
            @Parameter(name = "termError", description = "term error, e.g. Quarter must be entered in the correct format") @RequestParam String termError)
            throws JsonProcessingException {

        School school = School.builder()
                .abbrev(abbrev)
                .name(name)
                .termRegex(termRegex)
                .termDescription(termDescription)
                .termError(termError)
                .build();

        School savedSchool = schoolRepository.save(school);

        return savedSchool;
    }

    @Operation(summary = "Update a school")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update")
    public School updateSchool(
            @Parameter(name = "abbrev", description ="school abbrevation, e.g. ucsb" ) 
            @RequestParam String abbrev,
            @RequestBody @Valid School incoming) {

        School school = schoolRepository.findByAbbrev(abbrev)
                .orElseThrow(() -> new EntityNotFoundException(School.class, abbrev));
        
        school.setName(incoming.getName());
        school.setTermRegex(incoming.getTermRegex());
        school.setTermDescription(incoming.getTermDescription());
        school.setTermError(incoming.getTermError());

        schoolRepository.save(school);

        return school;
    }

    @Operation(summary = "Delete a school")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete")
    public Object deleteSchool(
            @Parameter(name = "abbrev", description ="school abbrevation, e.g. ucsb" ) 
            @RequestParam String abbrev) 
            throws JsonProcessingException {

        School school = schoolRepository.findByAbbrev(abbrev)
                .orElseThrow(() -> new EntityNotFoundException(School.class, abbrev));
        
        schoolRepository.delete(school);

        return genericMessage("School with abbrev %s deleted.".formatted(abbrev));
    }

}