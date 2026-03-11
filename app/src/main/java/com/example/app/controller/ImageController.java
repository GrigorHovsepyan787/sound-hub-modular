package com.example.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;

@Controller
@Slf4j
public class ImageController {
    @Value("${sound-hub-modular.upload.image.directory.path}")
    private String imageDirectoryPath;

    @GetMapping("/image/get")
    public @ResponseBody byte[] getImage(@RequestParam("picName") String picName) {
        File file = new File(imageDirectoryPath + picName);
        if (file.exists() && file.isFile()) {
            try {
                return FileUtils.readFileToByteArray(file);
            } catch (IOException e) {
                log.error("Error while getting picture of user {}, {}, {}", picName, e.getMessage(), e.getStackTrace());
            }
        }
        return null;
    }
}
