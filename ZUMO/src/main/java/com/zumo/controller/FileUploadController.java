package com.zumo.controller;

import com.zumo.dto.Result;
import com.zumo.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@Slf4j
public class FileUploadController {
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws Exception {
        log.info("文件上传: {}", file.getOriginalFilename());
        String originalFilename = file.getOriginalFilename();
        //保证文件名字唯一
        String filename = UUID.randomUUID().toString()+originalFilename.substring(originalFilename.lastIndexOf("."));
        //file.transferTo(new File("C:\\Users\\86187\\Desktop\\files\\"+filename));
        String url = AliOssUtil.uploadFile(filename, file.getInputStream());
        return Result.success(url);
    }
}