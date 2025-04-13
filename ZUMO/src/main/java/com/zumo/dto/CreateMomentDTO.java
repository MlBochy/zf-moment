package com.zumo.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateMomentDTO {
    private String content;
    private List<String> images;
} 