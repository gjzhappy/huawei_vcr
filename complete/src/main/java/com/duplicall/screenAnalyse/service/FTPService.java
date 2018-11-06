package com.duplicall.screenAnalyse.service;



import com.duplicall.screenAnalyse.commons.exceptions.FTPErrorsException;

import java.io.File;

/**
 * Created by gjz on 2017-10-11.
 */
public interface FTPService {
    void connectToFTP(String host, int port, String user, String pass) throws FTPErrorsException;

    boolean uploadFileToFTP(File file, String ftpHostDir, String serverFilename) throws FTPErrorsException;

    boolean downloadFileFromFTP(String ftpRelativePath, String copytoPath) throws FTPErrorsException;

    void disconnectFTP() throws FTPErrorsException;

    boolean existFile(String path, String fileName);
}
