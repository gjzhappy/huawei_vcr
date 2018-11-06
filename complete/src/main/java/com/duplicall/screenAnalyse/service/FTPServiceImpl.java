package com.duplicall.screenAnalyse.service;

import com.duplicall.screenAnalyse.commons.exceptions.FTPErrorMessage;
import com.duplicall.screenAnalyse.commons.exceptions.FTPErrorsException;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * Created by gjz on 2017-10-11.
 */
@Service
public class FTPServiceImpl implements FTPService {

    /**
     * FTP connection handler
     */
    FTPClient ftpconnection = null;

    private Logger logger = LoggerFactory.getLogger(FTPServiceImpl.class);

    /**
     * Method that implement FTP connection.
     *
     * @param host IP of FTP server
     * @param user FTP valid user
     * @param pass FTP valid pass for user
     * @throws FTPErrorsException Set of possible errors associated with connection process.
     */
    @Override
    public void connectToFTP(String host, int port, String user, String pass) throws FTPErrorsException {

        ftpconnection = new FTPClient();
        ftpconnection.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;

        try {
            ftpconnection.connect(host, port);
        } catch (IOException e) {
            FTPErrorMessage errorMessage = new FTPErrorMessage(-1, "It was not possible to connect to the ftp, host =" + host);
            logger.error(errorMessage.toString());
            throw new FTPErrorsException(errorMessage);
        }

        reply = ftpconnection.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply)) {

            try {
                ftpconnection.disconnect();
            } catch (IOException e) {
                FTPErrorMessage errorMessage = new FTPErrorMessage(-2, "It was not possible to connect to the ftp, host =" + host + " ,reply=" + reply);
                logger.error(errorMessage.toString());
                throw new FTPErrorsException(errorMessage);
            }
        }

        try {
            ftpconnection.login(user, pass);
        } catch (IOException e) {
            FTPErrorMessage errorMessage = new FTPErrorMessage(-3, "The User=" + user + ", Pass = * * * were not valid for authentication.");
            logger.error(errorMessage.toString());
            throw new FTPErrorsException(errorMessage);
        }

        try {
            ftpconnection.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
            FTPErrorMessage errorMessage = new FTPErrorMessage(-4, "The Data type for the transfer is invalid.");
            logger.error(errorMessage.toString());
            throw new FTPErrorsException(errorMessage);
        }

        ftpconnection.enterLocalPassiveMode();
    }

    /**
     * Method that allow upload file to FTP
     *
     * @param file           File object of file to upload
     * @param ftpHostDir     FTP host internal directory to save file
     * @param serverFilename Name to put the file in FTP server.
     * @throws FTPErrorsException Set of possible errors associated with upload process.
     */
    @Override
    public boolean uploadFileToFTP(File file, String ftpHostDir, String serverFilename) throws FTPErrorsException {
        InputStream input = null;
        boolean flag = false;
        try {
            input = new FileInputStream(file);
            flag = this.ftpconnection.storeFile(ftpHostDir + serverFilename, input);
        } catch (IOException e) {
            FTPErrorMessage errorMessage = new FTPErrorMessage(-5, "Could not upload the file to the server.");
            logger.error(errorMessage.toString());
            throw new FTPErrorsException(errorMessage);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        return flag;

    }

    /**
     * Method for download files from FTP.
     *
     * @param ftpRelativePath Relative path of file to download into FTP server.
     * @param copytoPath      Path to copy the file in download process.
     * @throws FTPErrorsException Set of errors associated with download process.
     */

    @Override
    public boolean downloadFileFromFTP(String ftpRelativePath, String copytoPath) throws FTPErrorsException {
        FileOutputStream fos;
        boolean flag = false;
        try {
            fos = new FileOutputStream(copytoPath);
        } catch (FileNotFoundException e) {
            FTPErrorMessage errorMessage = new FTPErrorMessage(-6, "Could not obtain the reference to the folder on the route and where to save, check permissions.");
            logger.error(errorMessage.toString());
            throw new FTPErrorsException(errorMessage);
        }
        try {
            flag = this.ftpconnection.retrieveFile(ftpRelativePath, fos);
        } catch (IOException e) {
            FTPErrorMessage errorMessage = new FTPErrorMessage(-7, "Could not download the file.");
            logger.error(errorMessage.toString());
            throw new FTPErrorsException(errorMessage);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        return flag;
    }

    /**
     * Method for release the FTP connection.
     *
     * @throws FTPErrorsException Error if unplugged process failed.
     */
    @Override
    public void disconnectFTP() throws FTPErrorsException {
        if (this.ftpconnection.isConnected()) {
            try {
                this.ftpconnection.logout();
                this.ftpconnection.disconnect();
            } catch (IOException f) {
                throw new FTPErrorsException(new FTPErrorMessage(-8, "An error has occurred while performing the disconnection of the FTP Server."));
            }
        }
    }

    /**
     * @param path     文件路径
     * @param fileName 文件名
     * @return
     */
    @Override
    public boolean existFile(String path, String fileName) {
        boolean flag = false;
        //提取目录和文件名
        try {
            ftpconnection.changeWorkingDirectory("/");
            ftpconnection.changeWorkingDirectory(path);
            FTPFile[] files = ftpconnection.listFiles(fileName);
            if (files.length > 0) {
                flag = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

}