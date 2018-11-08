package com.duplicall.screenAnalyse.commons.utils;

import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.exception.ObsException;
import com.obs.services.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.duplicall.screenAnalyse.commons.constants.ApplicationConstants.FileLocation.localBase;
import static com.duplicall.screenAnalyse.commons.constants.ApplicationConstants.HuaweiBulaBula.endPoint;
import static com.duplicall.screenAnalyse.commons.constants.ApplicationConstants.HuaweiBulaBula.ak;
import static com.duplicall.screenAnalyse.commons.constants.ApplicationConstants.HuaweiBulaBula.sk;
import static com.duplicall.screenAnalyse.commons.constants.ApplicationConstants.HuaweiBulaBula.bucketName;

public class OBSUtil {
//    private static final String endPoint = "https://obs.cn-north-1.myhwclouds.com";

//    private static final String ak = "OFNDQL7EHQZTDINVG6Y6";

//    private static final String sk = "ioE5urzKgFoFM79jRJX0EcwxSo7kxTM8cExHt6ja";

    private static ObsClient obsClient;

//    private static String bucketName = "obs-minist";

    private static String objectKey = "";

    private static String localFilePath;

//    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

//    private static List<PartEtag> partETags = Collections.synchronizedList(new ArrayList<PartEtag>());

    private static Logger logger = LoggerFactory.getLogger(OBSUtil.class);

    public static String upload(File sampleFile) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<PartEtag> partETags = Collections.synchronizedList(new ArrayList<PartEtag>());
        objectKey = sampleFile.getName();
        ObsConfiguration config = new ObsConfiguration();
        config.setSocketTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setEndPoint(endPoint);
        try {
            /*
             * Constructs a obs client instance with your account for accessing OBS
             */
            obsClient = new ObsClient(ak, sk, config);

            /*
             * Create bucket
             */
//            logger.info("Create a new bucket for demo\n");
//            obsClient.createBucket(bucketName);

            /*
             * Claim a upload id firstly
             */
            String uploadId = claimUploadId();
            logger.info("Claiming a new upload id " + uploadId + "\n");

            long partSize = 5 * 1024 * 1024l;// 5MB
//            File sampleFile = createSampleFile();
            long fileLength = sampleFile.length();

            long partCount = fileLength % partSize == 0 ? fileLength / partSize : fileLength / partSize + 1;

            if (partCount > 10000) {
                throw new RuntimeException("Total parts count should not exceed 10000");
            } else {
                logger.info("Total parts count " + partCount + "\n");
            }

            /*
             * Upload multiparts to your bucket
             */
            logger.info("Begin to upload multiparts to OBS from a file\n");
            for (int i = 0; i < partCount; i++) {
                long offset = i * partSize;
                long currPartSize = (i + 1 == partCount) ? fileLength - offset : partSize;
                executorService.execute(new OBSUtil.PartUploader(sampleFile, offset, currPartSize, i + 1, uploadId, partETags));
            }

            /*
             * Waiting for all parts finished
             */
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                try {
                    executorService.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /*
             * Verify whether all parts are finished
             */
            if (partETags.size() != partCount) {
                throw new IllegalStateException("Upload multiparts fail due to some parts are not finished yet");
            } else {
                logger.info("Succeed to complete multiparts into an object named " + objectKey + "\n");
            }

            /*
             * View all parts uploaded recently
             */
//            listAllParts(uploadId);

            /*
             * Complete to upload multiparts
             */
            completeMultipartUpload(uploadId, partETags);

        } catch (ObsException e) {
            logger.error("Response Code: " + e.getResponseCode());
            logger.error("Error Message: " + e.getErrorMessage());
            logger.error("Error Code:       " + e.getErrorCode());
            logger.error("Request ID:      " + e.getErrorRequestId());
            logger.error("Host ID:           " + e.getErrorHostId());
        } finally {
            if (obsClient != null) {
                try {
                    /*
                     * Close obs client
                     */
                    obsClient.close();
                } catch (IOException e) {
                }
            }
        }
        return sampleFile.getName();
    }

    private static class PartUploader implements Runnable {

        private File sampleFile;

        private long offset;

        private long partSize;

        private int partNumber;

        private String uploadId;

        private List<PartEtag> partETags;

        public PartUploader(File sampleFile, long offset, long partSize, int partNumber, String uploadId, List<PartEtag> partETags) {
            this.sampleFile = sampleFile;
            this.offset = offset;
            this.partSize = partSize;
            this.partNumber = partNumber;
            this.uploadId = uploadId;
            this.partETags = partETags;
        }

        @Override
        public void run() {
            try {
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setObjectKey(objectKey);
                uploadPartRequest.setUploadId(this.uploadId);
                uploadPartRequest.setFile(this.sampleFile);
                uploadPartRequest.setPartSize(this.partSize);
                uploadPartRequest.setOffset(this.offset);
                uploadPartRequest.setPartNumber(this.partNumber);

                UploadPartResult uploadPartResult = obsClient.uploadPart(uploadPartRequest);
                logger.info("Part#" + this.partNumber + " done\n");
                partETags.add(new PartEtag(uploadPartResult.getEtag(), uploadPartResult.getPartNumber()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String claimUploadId()
            throws ObsException {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);
        InitiateMultipartUploadResult result = obsClient.initiateMultipartUpload(request);
        return result.getUploadId();
    }

    private static File createSampleFile()
            throws IOException {
        File file = File.createTempFile("obs-java-sdk-", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        for (int i = 0; i < 1000000; i++) {
            writer.write(UUID.randomUUID() + "\n");
            writer.write(UUID.randomUUID() + "\n");
        }
        writer.flush();
        writer.close();

        return file;
    }

    private static void completeMultipartUpload(String uploadId, List<PartEtag> partETags)
            throws ObsException {
        // Make part numbers in ascending order
        Collections.sort(partETags, new Comparator<PartEtag>() {

            @Override
            public int compare(PartEtag o1, PartEtag o2) {
                return o1.getPartNumber() - o2.getPartNumber();
            }
        });

        logger.info("Completing to upload multiparts\n");
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, objectKey, uploadId, partETags);
        obsClient.completeMultipartUpload(completeMultipartUploadRequest);
    }

    public static void delete(String fileName) {
        String objectKey = fileName;
        ObsConfiguration config = new ObsConfiguration();
        config.setSocketTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setEndPoint(endPoint);
        try {
            /*
             * Constructs a obs client instance with your account for accessing OBS
             */
            obsClient = new ObsClient(ak, sk, config);
            obsClient.deleteObject(bucketName, objectKey, null);
        } catch (ObsException e) {
            logger.error("Response Code: " + e.getResponseCode());
            logger.error("Error Message: " + e.getErrorMessage());
            logger.error("Error Code:       " + e.getErrorCode());
            logger.error("Request ID:      " + e.getErrorRequestId());
            logger.error("Host ID:           " + e.getErrorHostId());
        } finally {
            if (obsClient != null) {
                try {
                    /*
                     * Close obs client
                     */
                    obsClient.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String download(String taskId, String fileName)
            throws IOException {
        String objectKey = fileName;
        localFilePath = localBase + objectKey;
        ObsConfiguration config = new ObsConfiguration();
        config.setSocketTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setEndPoint(endPoint);
        try {
            /*
             * Constructs a obs client instance with your account for accessing OBS
             */
            obsClient = new ObsClient(ak, sk, config);

            /*
             * Download the object as an inputstream and display it directly
             */
//            simpleDownload();

            File localFile = new File(localFilePath);
            if (!localFile.getParentFile().exists()) {
                localFile.getParentFile().mkdirs();
            }

            logger.info("Downloading an object to file:" + localFilePath + "\n");
            /*
             * Download the object to a file
             */
            String act_objectKey = "output/" + taskId + "/" + bucketName + "/" + objectKey;
            downloadToLocalFile(act_objectKey);

            /*为方便,直接删除obs中的文件*/
            logger.info("Deleting object  " + act_objectKey + "\n");
            obsClient.deleteObject(bucketName, act_objectKey, null);
            return localFilePath;
        } catch (ObsException e) {
            logger.error("Response Code: " + e.getResponseCode());
            logger.error("Error Message: " + e.getErrorMessage());
            logger.error("Error Code:       " + e.getErrorCode());
            logger.error("Request ID:      " + e.getErrorRequestId());
            logger.error("Host ID:           " + e.getErrorHostId());
        } finally {
            if (obsClient != null) {
                try {
                    /*
                     * Close obs client
                     */
                    obsClient.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private static void downloadToLocalFile(String objectKey)
            throws ObsException, IOException {
        ObsObject obsObject = obsClient.getObject(bucketName, objectKey, null);
        ReadableByteChannel rchannel = Channels.newChannel(obsObject.getObjectContent());

        ByteBuffer buffer = ByteBuffer.allocate(4096);
        WritableByteChannel wchannel = Channels.newChannel(new FileOutputStream(new File(localFilePath)));
        while (rchannel.read(buffer) != -1) {
            buffer.flip();
            wchannel.write(buffer);
            buffer.clear();
        }
        rchannel.close();
        wchannel.close();
    }


    public static void main(String[] args) {
//        File file = new File("C:\\temp\\5bd896a0-ddf7-4461-bf53-93e4feb92e53.mp4");
//        File file = new File("C:\\temp\\5fdd73c8-6f67-448f-8eb9-86f6d070db57.mp4");
        try {
//            file = createSampleFile();
//            String fileName = upload(file);
//            System.out.println(fileName);
            download("taskm522j5kd", "f602125e-c9b6-4a6a-8c7d-1921eb682cd3.mp4.json");
//            delete("obs-java-sdk-2783098217226618676.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
