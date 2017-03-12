package com.airhacks.nano;

import com.sun.net.httpserver.Headers;

/**
 *
 * @author airhacks.com
 */
@FunctionalInterface
public interface NanoRequest {

    /* 
     * This interface is used by Invocable.getInterface() to provide an 
     * interface to the process method in duke.js.
     * Importantly, it has the same method signature as the method in duke.js, so 
     * an Invocable object's getInterface method can map it onto duke.js's process method.
     */

    int process(String method, Headers requestHeaders, Headers responseHeaders, String request, ResponseWriter response);
}
