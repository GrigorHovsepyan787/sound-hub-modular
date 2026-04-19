package com.example.mapper;

import com.example.dto.RegisterRequest;
import com.example.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegisterRequestMapper {
    @Mapping(target = "userStatus", constant = "UNENABLED")
    @Mapping(target = "userType", constant = "USER")
    User toEntity(RegisterRequest request);
}