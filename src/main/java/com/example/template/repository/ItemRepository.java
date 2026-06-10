package com.example.template.repository;

import com.example.template.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    Page<Item> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
