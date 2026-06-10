package com.example.template.api;

import com.example.template.api.dto.CreateItemRequest;
import com.example.template.api.dto.ItemResponse;
import com.example.template.api.dto.UpdateItemRequest;
import com.example.template.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Items", description = "CRUD operations for items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    @Operation(summary = "List all items (paginated)")
    public Page<ItemResponse> list(@ParameterObject Pageable pageable) {
        return itemService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    public ItemResponse getById(@PathVariable UUID id) {
        return itemService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new item")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
        ItemResponse created = itemService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace an item")
    public ItemResponse update(@PathVariable UUID id,
                               @Valid @RequestBody UpdateItemRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an item")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
