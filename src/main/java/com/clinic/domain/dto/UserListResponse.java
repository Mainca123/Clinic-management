package com.clinic.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserListResponse {

    private List<UserResponse> users;

    private long totalItems;

    private int totalPages;

    private int currentPage;

    private int pageSize;
}