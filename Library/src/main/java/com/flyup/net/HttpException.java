package com.flyup.net;
/**
 *
 * Created by focux on 2016-3-23 .
 */
public class HttpException extends Exception {

    public int code;

    public HttpException() {
        super();
    }


    public HttpException(int code) {
        super();
        this.code = code;
    }
    public HttpException(int code,String detailMessage) {
        super(detailMessage);
        this.code = code;
    }
    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return "code:" + code + "case:" + getCause() + "\n" + super.toString();
    }
}
