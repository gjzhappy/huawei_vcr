package com.duplicall.screenAnalyse.commons.exceptions;

/**
 * Created by gjz on 2017-10-11.
 */
public class FTPErrorsException extends Exception {

    private FTPErrorMessage errorMessage;

    public FTPErrorsException(FTPErrorMessage errorMessage) {
        super(errorMessage.getErrormessage());
    }

    public FTPErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
