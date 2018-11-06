package com.duplicall.screenAnalyse.commons.exceptions;

/**
 * Created by gjz on 2017-10-11.
 */
public class FTPErrorMessage {

    private int errorcode;
    private String errormessage;

    public FTPErrorMessage(int errorcode, String errormessage) {
        this.errorcode = errorcode;
        this.errormessage = errormessage;
    }

    public int getErrorcode() {
        return errorcode;
    }

    public String getErrormessage() {
        return errormessage;
    }

    @Override
    public String toString() {
        return "FTPErrorMessage{" +
                "errorcode=" + errorcode +
                ", errormessage='" + errormessage + '\'' +
                '}';
    }
}
