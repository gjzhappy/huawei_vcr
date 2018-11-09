package com.duplicall.screenAnalyse.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.duplicall.screenAnalyse.commons.constants.ApplicationConstants;
import com.duplicall.screenAnalyse.commons.exceptions.FTPErrorsException;
import com.duplicall.screenAnalyse.commons.pojo.ScreenVideo;
import com.duplicall.screenAnalyse.commons.utils.OBSUtil;
import com.duplicall.screenAnalyse.commons.utils.VideoOcrUtil;
import com.duplicall.screenAnalyse.service.FTPServiceImpl;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.duplicall.screenAnalyse.commons.constants.ApplicationConstants.FileLocation.localBase;
import static com.duplicall.screenAnalyse.commons.constants.ApplicationConstants.HuaweiBulaBula.*;
import static com.duplicall.screenAnalyse.commons.constants.ApplicationConstants.esUrl;
import static com.duplicall.screenAnalyse.commons.utils.OcrTransUtil.ocr_fragments;
import static com.duplicall.screenAnalyse.commons.utils.OcrTransUtil.trace_auditrail;
import static com.duplicall.screenAnalyse.commons.utils.VideoOcrUtil.*;

public class ScreenAnalyseTask implements Runnable {
    public ScreenAnalyseTask(ScreenVideo screenVideo) {
        this.screenVideo = screenVideo;
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ScreenVideo screenVideo;

    @Override
    public void run() {
        logger.info("________ScreenAnalyseTask {} start_________", Thread.currentThread().getName());
        logger.info("request body: {}", screenVideo.toString());
        logger.info("<<<<download mp4 & trace file from ftp>>>>");
        /*一.从ftp下载mp4及trace文件到本地*/
        FTPServiceImpl ftpService = new FTPServiceImpl();
        String tempFileName = UUID.randomUUID().toString().replaceAll("-", "");
        try {
            ftpService.connectToFTP(ApplicationConstants.Ftp.host, ApplicationConstants.Ftp.port, ApplicationConstants.Ftp.username, ApplicationConstants.Ftp.password);
            /*下载 screen video 文件*/
            ftpService.downloadFileFromFTP(screenVideo.getVideo_file_ftp(), localBase + tempFileName + ".mp4");
            /*下载 screen xml 文件*/
            ftpService.downloadFileFromFTP(screenVideo.getVideo_file_xml(), localBase + tempFileName + ".xml");
        } catch (FTPErrorsException e) {
            logger.error(e.getLocalizedMessage(), e);
            return;
        }

        logger.info("<<<<upload local mp4 to obs>>>>");
        /*二.将本地的mp4文件上传到obs*/
        File file = new File(localBase + tempFileName + ".mp4");
        if (file.exists()) {
            OBSUtil.upload(file);
            file.delete();/*本地文件删除*/
        }
        logger.info("<<<<create ocr task>>>>");
        /*三.创建ocr作业*/
        String token, taskId;
        try {
            token = getToken(ApplicationConstants.HuaweiBulaBula.username, ApplicationConstants.HuaweiBulaBula.password, projectName);
//            logger.info("token is {}",token);
            taskId = requestVideoOcr(token, projectId, UUID.randomUUID() + "-video-ocr-task", bucketName, tempFileName + ".mp4");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return;
        }
        logger.info("<<<<monitor ocr task...>>>>");
        /*四.监听ocr作业是否完成*/
        boolean finished = false;
        while (!finished) {
            Boolean res = null;
            try {
                res = getVideoOcrStatus(token, projectId, taskId);
                if (res) {
                    finished = true;
                }
            } catch (Exception e) {
                logger.info("read ocr task status timeout.");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("<<<<video ocr task finished,download ocr result,delete task and obs file>>>>");
        /*五.下载ocr作业结果,删除作业记录及obs文件*/
        try {
            String fileName = tempFileName + ".mp4.json";
            OBSUtil.download(taskId, fileName);
            OBSUtil.delete(tempFileName + ".mp4");
            VideoOcrUtil.deleteVideoOcr(token, projectId, taskId);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("<<<<analyse ocr result ,put it into es engine>>>>");
        /*六.分析ocr结果,将结果存进es搜索引擎*/
        try {
            postToEsServer(tempFileName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("put ocr result into es engine error!");
        }


        logger.info("________ScreenAnalyseTask {} stop_________", Thread.currentThread().getName());
    }

    private void postToEsServer(String tempFileName) throws Exception {
        JSONArray fragments = ocr_fragments(localBase + tempFileName + ".mp4.json");
        JSONArray audit_trails = trace_auditrail(localBase + tempFileName + ".xml");
        JSONObject requestBody = new JSONObject();
        requestBody.put("startrecordtime", screenVideo.getStartReordTime());
        requestBody.put("extension", screenVideo.getExtension());
        requestBody.put("callerid", screenVideo.getCallerId());
        requestBody.put("calledid", screenVideo.getCalledId());
        requestBody.put("media_type", "01");
        requestBody.put("platform", "001");
        requestBody.put("video_file", screenVideo.getVideo_file_url());
        requestBody.put("fragments", fragments);
        requestBody.put("audit-trails", audit_trails);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String uri = esUrl + screenVideo.getReferenceId();
        HttpPost httpPost = new HttpPost(uri);
        Header header = new BasicHeader("Content-Type", "application/json");
        httpPost.addHeader(header);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .build();
        httpPost.setConfig(requestConfig);
        StringEntity stringEntity = new StringEntity(requestBody.toJSONString(), "utf-8");
        httpPost.setEntity(stringEntity);
        HttpResponse response = httpClient.execute(httpPost);
        System.out.println(response);
    }
}
