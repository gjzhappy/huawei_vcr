//package com.duplicall.screenAnalyse.commons.utils;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.Writer;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import com.obs.services.ObsClient;
//import com.obs.services.ObsConfiguration;
//import com.obs.services.exception.ObsException;
//import com.obs.services.pojo.CompleteMultipartUploadRequest;
//import com.obs.services.pojo.InitiateMultipartUploadRequest;
//import com.obs.services.pojo.InitiateMultipartUploadResult;
//import com.obs.services.pojo.ListPartsRequest;
//import com.obs.services.pojo.ListPartsResult;
//import com.obs.services.pojo.Multipart;
//import com.obs.services.pojo.PartEtag;
//import com.obs.services.pojo.UploadPartRequest;
//import com.obs.services.pojo.UploadPartResult;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * This sample demonstrates how to multipart upload an object concurrently
// * from OBS using the OBS SDK for Java.
// */
//public class ConcurrentUploadPartSample {
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
//    private static String objectKey = "";
//
//    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
//
//    private static List<PartEtag> partETags = Collections.synchronizedList(new ArrayList<PartEtag>());
//
//    private static Logger logger = LoggerFactory.getLogger(ConcurrentUploadPartSample.class);
//
//    public static String upload(File sampleFile)
//            throws IOException {
//        objectKey = sampleFile.getName();
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
//             * Create bucket
//             */
////            logger.info("Create a new bucket for demo\n");
////            obsClient.createBucket(bucketName);
//
//            /*
//             * Claim a upload id firstly
//             */
//            String uploadId = claimUploadId();
//            logger.info("Claiming a new upload id " + uploadId + "\n");
//
//            long partSize = 5 * 1024 * 1024l;// 5MB
////            File sampleFile = createSampleFile();
//            long fileLength = sampleFile.length();
//
//            long partCount = fileLength % partSize == 0 ? fileLength / partSize : fileLength / partSize + 1;
//
//            if (partCount > 10000) {
//                throw new RuntimeException("Total parts count should not exceed 10000");
//            } else {
//                logger.info("Total parts count " + partCount + "\n");
//            }
//
//            /*
//             * Upload multiparts to your bucket
//             */
//            logger.info("Begin to upload multiparts to OBS from a file\n");
//            for (int i = 0; i < partCount; i++) {
//                long offset = i * partSize;
//                long currPartSize = (i + 1 == partCount) ? fileLength - offset : partSize;
//                executorService.execute(new PartUploader(sampleFile, offset, currPartSize, i + 1, uploadId));
//            }
//
//            /*
//             * Waiting for all parts finished
//             */
//            executorService.shutdown();
//            while (!executorService.isTerminated()) {
//                try {
//                    executorService.awaitTermination(5, TimeUnit.SECONDS);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            /*
//             * Verify whether all parts are finished
//             */
//            if (partETags.size() != partCount) {
//                throw new IllegalStateException("Upload multiparts fail due to some parts are not finished yet");
//            } else {
//                logger.info("Succeed to complete multiparts into an object named " + objectKey + "\n");
//            }
//
//            /*
//             * View all parts uploaded recently
//             */
////            listAllParts(uploadId);
//
//            /*
//             * Complete to upload multiparts
//             */
//            completeMultipartUpload(uploadId);
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
//        return sampleFile.getName();
//    }
//
//    private static class PartUploader implements Runnable {
//
//        private File sampleFile;
//
//        private long offset;
//
//        private long partSize;
//
//        private int partNumber;
//
//        private String uploadId;
//
//        public PartUploader(File sampleFile, long offset, long partSize, int partNumber, String uploadId) {
//            this.sampleFile = sampleFile;
//            this.offset = offset;
//            this.partSize = partSize;
//            this.partNumber = partNumber;
//            this.uploadId = uploadId;
//        }
//
//        @Override
//        public void run() {
//            try {
//                UploadPartRequest uploadPartRequest = new UploadPartRequest();
//                uploadPartRequest.setBucketName(bucketName);
//                uploadPartRequest.setObjectKey(objectKey);
//                uploadPartRequest.setUploadId(this.uploadId);
//                uploadPartRequest.setFile(this.sampleFile);
//                uploadPartRequest.setPartSize(this.partSize);
//                uploadPartRequest.setOffset(this.offset);
//                uploadPartRequest.setPartNumber(this.partNumber);
//
//                UploadPartResult uploadPartResult = obsClient.uploadPart(uploadPartRequest);
//                logger.info("Part#" + this.partNumber + " done\n");
//                partETags.add(new PartEtag(uploadPartResult.getEtag(), uploadPartResult.getPartNumber()));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private static String claimUploadId()
//            throws ObsException {
//        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);
//        InitiateMultipartUploadResult result = obsClient.initiateMultipartUpload(request);
//        return result.getUploadId();
//    }
//
//    private static File createSampleFile()
//            throws IOException {
//        File file = File.createTempFile("obs-java-sdk-", ".txt");
//        file.deleteOnExit();
//
//        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
//        for (int i = 0; i < 1000000; i++) {
//            writer.write(UUID.randomUUID() + "\n");
//            writer.write(UUID.randomUUID() + "\n");
//        }
//        writer.flush();
//        writer.close();
//
//        return file;
//    }
//
//    private static void completeMultipartUpload(String uploadId)
//            throws ObsException {
//        // Make part numbers in ascending order
//        Collections.sort(partETags, new Comparator<PartEtag>() {
//
//            @Override
//            public int compare(PartEtag o1, PartEtag o2) {
//                return o1.getPartNumber() - o2.getPartNumber();
//            }
//        });
//
//        logger.info("Completing to upload multiparts\n");
//        CompleteMultipartUploadRequest completeMultipartUploadRequest =
//                new CompleteMultipartUploadRequest(bucketName, objectKey, uploadId, partETags);
//        obsClient.completeMultipartUpload(completeMultipartUploadRequest);
//    }
//
//    private static void listAllParts(String uploadId)
//            throws ObsException {
//        logger.info("Listing all parts......");
//        ListPartsRequest listPartsRequest = new ListPartsRequest(bucketName, objectKey, uploadId);
//        ListPartsResult partListing = obsClient.listParts(listPartsRequest);
//
//        for (Multipart part : partListing.getMultipartList()) {
//            logger.info("\tPart#" + part.getPartNumber() + ", ETag=" + part.getEtag());
//        }
//    }
//
//    public static void main(String[] args) {
//        try {
//            File file = createSampleFile();
//            String fileName = upload(file);
//            System.out.println(fileName);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
