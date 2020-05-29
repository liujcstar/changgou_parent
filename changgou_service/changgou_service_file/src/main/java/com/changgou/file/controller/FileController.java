package com.changgou.file.controller;


import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.file.util.FastDFSClient;
import com.changgou.file.util.FastDFSFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {


    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws IOException {

        //获取文件名
        String filename = file.getOriginalFilename();

        //获取文件后缀名
        String ext = filename.substring(filename.lastIndexOf(".") + 1);

        //获取文件数据
        byte[] bytes = file.getBytes();

        //使用工具类上传数据
        FastDFSFile fastDFSFile = new FastDFSFile(filename,bytes,ext);

        String[] upload = FastDFSClient.upload(fastDFSFile);

        //返回图片路径
        String url = FastDFSClient.getTrackerUrl() + upload[0]+"/"+upload[1];

        //返回结果
        return new Result(true, StatusCode.OK,"文件上传成功",url);

    }

}
