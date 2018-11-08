//package com.duplicall.screenAnalyse.commons.utils;
//
//import com.obs.services.ObsClient;
//import com.obs.services.ObsConfiguration;
//import com.obs.services.exception.ObsException;
//import com.obs.services.pojo.ObsObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.channels.Channels;
//import java.nio.channels.ReadableByteChannel;
//import java.nio.channels.WritableByteChannel;
//
///**
// * This sample demonstrates how to download an object
// * from OBS in different ways using the OBS SDK for Java.
// */
//public class DownloadSample {
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
//    private static String localFilePath;
//
//    private static Logger logger = LoggerFactory.getLogger(DownloadSample.class);
//
//    public static void download(String fileName)
//            throws IOException {
//        objectKey = fileName;
//        localFilePath = "c:/temp/" + objectKey;
//        ObsConfiguration config = new ObsConfiguration();
//        config.setSocketTimeout(30000);
//        config.setConnectionTimeout(10000);
//        config.setEndPoint(endPoint);
//        try {
//            /*
//             * Constructs a obs client instance with your account for accessing OBS
//             */
//            obsClient = new ObsClient(ak, sk, config);
//
//            /*
//             * Download the object as an inputstream and display it directly
//             */
////            simpleDownload();
//
//            File localFile = new File(localFilePath);
//            if (!localFile.getParentFile().exists()) {
//                localFile.getParentFile().mkdirs();
//            }
//
//            logger.info("Downloading an object to file:" + localFilePath + "\n");
//            /*
//             * Download the object to a file
//             */
//            downloadToLocalFile();
//
//            /*为方便,直接删除*/
//            logger.info("Deleting object  " + objectKey + "\n");
//            obsClient.deleteObject(bucketName, objectKey, null);
//
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
//    private static void downloadToLocalFile()
//            throws ObsException, IOException {
//        ObsObject obsObject = obsClient.getObject(bucketName, objectKey, null);
//        ReadableByteChannel rchannel = Channels.newChannel(obsObject.getObjectContent());
//
//        ByteBuffer buffer = ByteBuffer.allocate(4096);
//        WritableByteChannel wchannel = Channels.newChannel(new FileOutputStream(new File(localFilePath)));
//
//        while (rchannel.read(buffer) != -1) {
//            buffer.flip();
//            wchannel.write(buffer);
//            buffer.clear();
//        }
//        rchannel.close();
//        wchannel.close();
//    }
//
//    public static void main(String[] args) {
//        try {
//            download("obs-java-sdk-5826040736243068123.txt");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
