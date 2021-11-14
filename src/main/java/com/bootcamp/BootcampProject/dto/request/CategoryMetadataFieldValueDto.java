package com.bootcamp.BootcampProject.dto.request;

import javax.validation.constraints.NotNull;
import java.util.List;

public class CategoryMetadataFieldValueDto {
    @NotNull(message = "fieldValues should not be null!!")
    //private List<String> fieldValues;
    private String fieldValues;

    public String getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(String fieldValues) {
        this.fieldValues = fieldValues;
    }
}
