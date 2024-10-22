package com.jh.coincoin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

/**
 * Created by dale on 2024-10-22.
 */

@Entity
@Getter
@NoArgsConstructor
@Table(name = "admin")
public class AdminEntity implements Persistable<Integer> {

    @Id
    @Column(name = "idx")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;
    @Column(name = "name")
    private String name;
    @Column(name = "value")
    private String value;

    public void changeValue(String newValue) {
        value = newValue;
    }

    @Override
    public Integer getId() {
        return idx;
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
