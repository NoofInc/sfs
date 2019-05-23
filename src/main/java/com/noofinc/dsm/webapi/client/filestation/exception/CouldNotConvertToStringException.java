package com.noofinc.dsm.webapi.client.filestation.exception;

import com.noofinc.dsm.webapi.client.core.exception.DsmWebApiErrorException;

public class CouldNotConvertToStringException extends DsmWebApiErrorException {

    public CouldNotConvertToStringException(Throwable cause) {
        super("Error converting object to string", cause, null);
    }
}
