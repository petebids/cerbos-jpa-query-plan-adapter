package com.github.petebids;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "nested")
@Data
@Entity
public class Nested {
    @Id
    private String id;
    private Boolean aBool;
}
