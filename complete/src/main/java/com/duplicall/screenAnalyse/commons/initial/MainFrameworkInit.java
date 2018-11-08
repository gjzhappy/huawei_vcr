package com.duplicall.screenAnalyse.commons.initial;

import com.duplicall.screenAnalyse.commons.constants.ApplicationConstants;
import com.duplicall.screenAnalyse.commons.utils.ConfigReader;
import com.duplicall.screenAnalyse.commons.utils.XmlUtil;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class MainFrameworkInit implements ApplicationListener<ContextRefreshedEvent> {
    protected static final Logger logger = LoggerFactory.getLogger(MainFrameworkInit.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            try {
                new ConfigReader().initFileLocation();
                initParams();
                initTempFile();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                System.exit(0);
            }

        }
    }

    private void initTempFile() {
        File temp = new File("c:/temp");
        if (!temp.exists()) {
            temp.mkdirs();
        }
    }

    private void initParams() throws Exception {
        XmlUtil xmlUtil = new XmlUtil();
        xmlUtil.readFile(ApplicationConstants.FileLocation.baseLocation + ApplicationConstants.FileName.SCREEN_VCR_FILE_NAME);
        //huawei 参数
        ApplicationConstants.HuaweiBulaBula.endPoint = xmlUtil.getText("//screen_vcr/huawei/endPoint");
        ApplicationConstants.HuaweiBulaBula.projectId = xmlUtil.getText("//screen_vcr/huawei/projectId");
        ApplicationConstants.HuaweiBulaBula.projectName = xmlUtil.getText("//screen_vcr/huawei/projectName");
        ApplicationConstants.HuaweiBulaBula.bucketName = xmlUtil.getText("//screen_vcr/huawei/bucketName");
        ApplicationConstants.HuaweiBulaBula.username = xmlUtil.getText("//screen_vcr/huawei/username");
        ApplicationConstants.HuaweiBulaBula.password = xmlUtil.getText("//screen_vcr/huawei/password");
        ApplicationConstants.HuaweiBulaBula.ak = xmlUtil.getText("//screen_vcr/huawei/ak");
        ApplicationConstants.HuaweiBulaBula.sk = xmlUtil.getText("//screen_vcr/huawei/sk");
        //ftp 参数
        ApplicationConstants.Ftp.host = xmlUtil.getAttribute("//screen_vcr/ftp", "host");
        ApplicationConstants.Ftp.port = Integer.valueOf(xmlUtil.getAttribute("//screen_vcr/ftp", "port"));
        ApplicationConstants.Ftp.username = xmlUtil.getAttribute("//screen_vcr/ftp", "username");
        ApplicationConstants.Ftp.password = xmlUtil.getAttribute("//screen_vcr/ftp", "password");
        //other
        ApplicationConstants.FileLocation.localBase = xmlUtil.getText("//screen_vcr/localBase");
        ApplicationConstants.esUrl = xmlUtil.getText("//screen_vcr/es");
    }
}
