package com.mysocial.dto.group;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CreateGroupRequest {
    private String groupName;
    private String avatarUrl = null;
    private List<Long> memberIds = new ArrayList<>();
} 