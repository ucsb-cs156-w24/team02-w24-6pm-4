package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
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

@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

    @Autowired
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @Operation(summary= "List all Menu Items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBDiningCommonsMenuItem> allUCSBMenuItems() {
        Iterable<UCSBDiningCommonsMenuItem> menuItems = ucsbDiningCommonsMenuItemRepository.findAll();
        return menuItems;
    }

    @Operation(summary= "Create a new Menu Item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenuItem postUCSBMenuItem(
            @Parameter(name="name") @RequestParam String name,
            @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode,
            @Parameter(name="station") @RequestParam String station)
            throws JsonProcessingException {


        //log.info("localDateTime={}", localDateTime);

        UCSBDiningCommonsMenuItem ucsbMenuItem = new UCSBDiningCommonsMenuItem();
        ucsbMenuItem.setStation(station);
        ucsbMenuItem.setName(name);
        ucsbMenuItem.setDiningCommonsCode(diningCommonsCode);
        UCSBDiningCommonsMenuItem savedUcsbMenuItem = ucsbDiningCommonsMenuItemRepository.save(ucsbMenuItem);

        return savedUcsbMenuItem;
    }

    @Operation(summary= "Get a single Menu Item")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenuItem getById(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenuItem ucsbMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        return ucsbMenuItem;
    }

    @Operation(summary= "Delete a Menu Item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBMenuItem(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenuItem ucsbMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        ucsbDiningCommonsMenuItemRepository.delete(ucsbMenuItem);
        return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
    }

    @Operation(summary= "Update a single Menu Item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenuItem updateUCSBMenuItem(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

        UCSBDiningCommonsMenuItem ucsbMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        ucsbMenuItem.setName(incoming.getName());
        ucsbMenuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
        ucsbMenuItem.setStation(incoming.getStation());
        ucsbDiningCommonsMenuItemRepository.save(ucsbMenuItem);

        return ucsbMenuItem;
    }
}
