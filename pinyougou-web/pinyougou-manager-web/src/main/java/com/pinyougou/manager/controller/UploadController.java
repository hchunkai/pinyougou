package com.pinyougou.manager.controller;


import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UploadController {

    //加载配置文件中的地址
    @Value("${fileServerUrl}")
    private String fileServerUrl;

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile multipartFile) {

        //封装返回数据
        Map<String, Object> data = new HashMap<>();

        try {
            //初始化fastFSD
            String path = this.getClass().getResource("/fastdfs_client.conf").getPath();
            ClientGlobal.init(path);

            //获得上传文件的字节码文件
            byte[] bytes = multipartFile.getBytes();
            //获得上传文件的文件名
            String originalFilename = multipartFile.getOriginalFilename();

            StorageClient storageClient = new StorageClient();
            String[] strings = storageClient.upload_file(bytes, FilenameUtils.getExtension(originalFilename), null);
            StringBuilder stringBuilder = new StringBuilder(fileServerUrl);
            //拼接要返回的url图片地址
            for (String string : strings) {
                stringBuilder.append("/" + string);
            }
            data.put("url", stringBuilder.toString());
            data.put("status", 200);
        } catch (Exception e) {
        e.printStackTrace();
        }
        return data;
    }


}
