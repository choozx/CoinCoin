package com.jh.coincoin.repo;

import com.jh.coincoin.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by dale on 2024-10-22.
 */
public interface AdminRepository extends JpaRepository<AdminEntity, Integer> {

    AdminEntity findByName(String name);
}
