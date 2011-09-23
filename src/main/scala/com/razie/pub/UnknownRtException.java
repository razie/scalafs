/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package com.razie.pub;

/**
 * unknown exception - used temp in development when i can't find a better one
 * 
 * @author razvanc99
 */
public class UnknownRtException extends RuntimeException {

    public UnknownRtException() {
    }

    public UnknownRtException(String arg0) {
        super(arg0);
    }

    public UnknownRtException(Throwable arg0) {
        super(arg0);
    }

    public UnknownRtException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
