package com.example.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;

@Controller
@Slf4j
public class ImageController {
    @Value("${sound-hub-modular.upload.image.directory.path}")
    private String imageDirectoryPath;

    @GetMapping("/image/get")
    public ResponseEntity<byte[]> getImage(@RequestParam("picName") String picName) {
        File file = new File(imageDirectoryPath + picName);
        if (!file.exists()) {
            return  ResponseEntity.notFound().build();
        }
        try{
            byte[] image = FileUtils.readFileToByteArray(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(image);
        }catch (IOException e){
            log.error("Image get error {}", picName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
