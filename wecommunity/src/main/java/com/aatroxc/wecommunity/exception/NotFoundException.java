package com.aatroxc.wecommunity.exception;

import org.springframework.http.HttpStatus;

/**
 *
 *
 * @author mafei007
 * @date 2020/4/19 0:10
 */
public class NotFoundException extends CommunityException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
