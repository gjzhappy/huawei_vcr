package com.duplicall.screenAnalyse.task;

import com.duplicall.screenAnalyse.service.FTPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ScreenAnalyseTask implements Runnable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private FTPService ftpService;

    @Override
    public void run() {
        logger.info("________ScreenAnalyseTask {} start_________", Thread.currentThread().getName());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //todo 读ftp文件&调用华为ocr接口,解析xml,调用es接口


        logger.info("________ScreenAnalyseTask {} stop_________", Thread.currentThread().getName());

    }
}
