package com.navrotskyi.trippyapi.dto.admin;

import java.util.List;
import java.util.UUID;

public record NodeResponse(
    UUID id, 
    String name, 
    String note, 
    java.math.BigDecimal price, 
    boolean isSeparate,
    String reporterName,
    List<PostResponse> posts
) {}