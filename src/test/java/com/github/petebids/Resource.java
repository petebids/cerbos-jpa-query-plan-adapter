package com.github.petebids;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.util.Set;

@Data
@Entity
class Resource {

    @Id
    private String id;
    private String aString;
    private Boolean aBool;
    private Long aNumber;
    private String name;
    private String createdBy;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> ownedBy;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Nested nested;

}
