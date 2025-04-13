package com.zumo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MomentDTO {
    private Long id;
    private Long userId;
    private String content;
    private List<String> images;
    private Long liked;
    private LocalDateTime createTime;
} 