package com.github.petebids;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Nested {
    @Id
    private String id;
    private Boolean aBool;
}
