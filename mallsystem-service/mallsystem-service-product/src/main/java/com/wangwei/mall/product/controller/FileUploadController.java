package com.wangwei.mall.product.controller;

import com.wangwei.mall.common.result.Result;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Api("文件上传")
@RestController
@RequestMapping("admin/product")
@Slf4j
public class FileUploadController {

    //  获取文件上传对应的地址
    @Value("${minio.endpointUrl}")
    public String endpointUrl;

    @Value("${minio.accessKey}")
    public String accessKey;

    @Value("${minio.secreKey}")
    public String secreKey;

    @Value("${minio.bucketName}")
    public String bucketName;


    @ApiOperation("文件上传")
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception{

        //1. 准备获取到的文件上传的路径
        String url = "";
        //2. 使用MinIO服务的URL,端口,AccessKey 和 SecretKey 创建一个MinioClient对象
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(endpointUrl)
                        .credentials(accessKey, secreKey)
                        .build();

        //3. 检查存储桶是否已经存在
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (isExist){
            log.info("Bucket already exists.");
            log.info("存储桶已经存在。");
        }else {
            //4. 创建一个名为asiatrip的存储桶 用于存储照片的zip文件
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }

        //5. 定义一个文件的名称 :文件上传的时候 名称不能重复
        String fileName = System.currentTimeMillis()+ UUID.randomUUID().toString();

        //6. 使用putObject上传一个文件到存储桶中
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(),
                                file.getSize(),
                                -1)
                        .contentType(file.getContentType())
                        .build());

        //7. 文件上传之后的路径 http://172.16.4.21:9000/mall-new/xxxxx
        url = endpointUrl + "/" +bucketName + "/" +fileName;
        log.info("url:\t"+url);
        //  将文件上传之后的路径返回给页面！
        return Result.ok(url);

    }

}
