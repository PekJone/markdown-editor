package com.markdown.editor.controller;

import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.security.UserDetailsImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/upload")
@Api(tags = "文件上传", description = "单个/多个文件上传的接口")
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            
            Files.write(filePath, file.getBytes());
            
            String fileUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/multiple")
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication) {
        
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            List<String> fileUrls = new ArrayList<>();
            
            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                
                Files.write(filePath, file.getBytes());
                fileUrls.add("/uploads/" + fileName);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Files uploaded successfully", fileUrls));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to upload files: " + e.getMessage()));
        }
    }
}
