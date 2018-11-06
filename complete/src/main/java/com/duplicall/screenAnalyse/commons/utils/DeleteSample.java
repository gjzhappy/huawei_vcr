//package com.duplicall.screenAnalyse.commons.utils;
//
//import com.obs.services.ObsClient;
//import com.obs.services.ObsConfiguration;
//import com.obs.services.exception.ObsException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//
///**
// * This sample demonstrates how to delete an object
// * from OBS in different ways using the OBS SDK for Java.
// */
//public class DeleteSample {
//    private static final String endPoint = "https://obs.cn-north-1.myhwclouds.com";
//
//    private static final String ak = "OFNDQL7EHQZTDINVG6Y6";
//
//    private static final String sk = "ioE5urzKgFoFM79jRJX0EcwxSo7kxTM8cExHt6ja";
//
//    private static ObsClient obsClient;
//
//    private static String bucketName = "obs-minist";
//
//    private static String objectKey;
//
//    private static Logger logger = LoggerFactory.getLogger(DeleteSample.class);
//
//
//    public static void delete(String fileName) {
//        objectKey = fileName;
//        ObsConfiguration config = new ObsConfiguration();
//        config.setSocketTimeout(30000);
//        config.setConnectionTimeout(10000);
//        config.setEndPoint(endPoint);
//        try {
//            /*
//             * Constructs a obs client instance with your account for accessing OBS
//             */
//            obsClient = new ObsClient(ak, sk, config);
//            obsClient.deleteObject(bucketName, objectKey, null);
//        } catch (ObsException e) {
//            logger.error("Response Code: " + e.getResponseCode());
//            logger.error("Error Message: " + e.getErrorMessage());
//            logger.error("Error Code:       " + e.getErrorCode());
//            logger.error("Request ID:      " + e.getErrorRequestId());
//            logger.error("Host ID:           " + e.getErrorHostId());
//        } finally {
//            if (obsClient != null) {
//                try {
//                    /*
//                     * Close obs client
//                     */
//                    obsClient.close();
//                } catch (IOException e) {
//                }
//            }
//        }
//    }
//
//
//    public static void main(String[] args) {
//        try {
//            delete("obs-java-sdk-2963874401564579630.txt");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
