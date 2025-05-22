package com.mysocial.mapper;

import com.mysocial.dto.auth.request.RegisterRequest;
import com.mysocial.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "CUSTOMER")
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "biography", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "phone", source = "phone")
    User toUser(RegisterRequest registerRequest);
    void updateUserFromRequest(@MappingTarget User user, RegisterRequest request);

    // Custom method to update user with encoded password
    default User toUserWithEncodedPassword(RegisterRequest request, String encodedPassword) {
        User user = toUser(request);
        user.setPassword(encodedPassword);
        return user;
    }
}
