package com.mindskip.xzs.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class FileController {

    // 设置文件上传的固定目录
    private static final String UPLOAD_DIR = "C:\\Users\\32264\\Desktop\\upload\\";

    // 单文件上传
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please select a file to upload.");
        }

        try {
            // 确保目录存在
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件到指定路径
            String originalFilename = file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + originalFilename);
            Files.write(path, file.getBytes());

            // 返回上传的文件名
            return ResponseEntity.status(HttpStatus.OK).body("Uploaded: " + originalFilename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
        }
    }

    // 列出文件夹中的所有文件
    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles() {
        File folder = new File(UPLOAD_DIR);
        File[] files = folder.listFiles();

        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }
        }
        return ResponseEntity.ok(fileNames);
    }


  /*  @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        try {
            // 解码文件名，确保特殊字符和中文可以正确识别
            String decodedFileName = URLDecoder.decode(fileName, "UTF-8");
            Path filePath = Paths.get(UPLOAD_DIR + decodedFileName);
            byte[] fileBytes = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decodedFileName + "\"");

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }*/


    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("filename") String fileName) {
        try {
            // 解码文件名，确保特殊字符和中文可以识别
            String decodedFileName = URLDecoder.decode(fileName, "UTF-8");
            Path filePath = Paths.get(UPLOAD_DIR + decodedFileName);
            byte[] fileBytes = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decodedFileName + "\"");

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }



}
