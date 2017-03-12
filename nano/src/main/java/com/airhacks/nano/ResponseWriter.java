package com.airhacks.nano;

/**
 *
 * @author airhacks.com
 */
public interface ResponseWriter {
    /*
     * A very simple interface used when instantiating a StringBuilder 
     * that is passed to the HTTP handler so a response can be written.
     */
    void write(String content);
}
