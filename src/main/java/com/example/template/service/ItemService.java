package com.example.template.service;

import com.example.template.api.dto.CreateItemRequest;
import com.example.template.api.dto.ItemResponse;
import com.example.template.api.dto.UpdateItemRequest;
import com.example.template.domain.Item;
import com.example.template.exception.ResourceNotFoundException;
import com.example.template.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Page<ItemResponse> findAll(Pageable pageable) {
        return itemRepository.findAll(pageable).map(ItemResponse::from);
    }

    public ItemResponse findById(UUID id) {
        return itemRepository.findById(id)
                .map(ItemResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    @Transactional
    public ItemResponse create(CreateItemRequest request) {
        var item = new Item(request.name(), request.description());
        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse update(UUID id, UpdateItemRequest request) {
        var item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
        item.update(request.name(), request.description());
        return ItemResponse.from(item);
    }

    @Transactional
    public void delete(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item", id);
        }
        itemRepository.deleteById(id);
    }
}
