package org.wso2.carbon.mediator.publishevent;

/**
 * The type of exceptions used in the BAM Mediator
 */
public class PublishEventMediatorException extends Exception {

    private String message;

    public PublishEventMediatorException(String s, Throwable throwable) {
        super(s, throwable);
        this.message = s;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
