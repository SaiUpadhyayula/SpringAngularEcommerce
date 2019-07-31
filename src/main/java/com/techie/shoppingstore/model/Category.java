package com.techie.shoppingstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = "Category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category implements Serializable {
    @Id
    private Long id;
    @Field(type = FieldType.Text, fielddata = true)
    private String name;
    private List<String> possibleFacets;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPossibleFacets() {
        return possibleFacets;
    }

    public void setPossibleFacets(List<String> possibleFacets) {
        this.possibleFacets = possibleFacets;
    }
}