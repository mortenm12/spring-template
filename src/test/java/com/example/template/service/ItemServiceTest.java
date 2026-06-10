package com.example.template.service;

import com.example.template.api.dto.CreateItemRequest;
import com.example.template.api.dto.UpdateItemRequest;
import com.example.template.domain.Item;
import com.example.template.exception.ResourceNotFoundException;
import com.example.template.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    ItemService itemService;

    @Test
    void findAll_returnsPageOfResponses() {
        var item = new Item("Widget", "A widget");
        given(itemRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of(item)));

        var result = itemService.findAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Widget");
    }

    @Test
    void findById_whenNotFound_throwsResourceNotFoundException() {
        var id = UUID.randomUUID();
        given(itemRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void create_savesAndReturnsResponse() {
        var request = new CreateItemRequest("Widget", "A widget");
        var savedItem = new Item("Widget", "A widget");
        given(itemRepository.save(any(Item.class))).willReturn(savedItem);

        var result = itemService.create(request);

        assertThat(result.name()).isEqualTo("Widget");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void delete_whenNotFound_throwsResourceNotFoundException() {
        var id = UUID.randomUUID();
        given(itemRepository.existsById(id)).willReturn(false);

        assertThatThrownBy(() -> itemService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_whenFound_updatesAndReturnsResponse() {
        var id = UUID.randomUUID();
        var item = new Item("Old Name", "Old desc");
        var request = new UpdateItemRequest("New Name", "New desc");
        given(itemRepository.findById(id)).willReturn(Optional.of(item));

        var result = itemService.update(id, request);

        assertThat(result.name()).isEqualTo("New Name");
    }
}
