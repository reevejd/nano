function process(method, requestHeaders, responseHeaders, request, response) {
    /*
     * A very simple http handler. Accepts and prints the request, writes a response, returns status code 200.
     */
    print(requestHeaders.values());
    responseHeaders.add("hey", "ho");

    print(request);
    response.write(method + "echo: " + request);
    return 200;
}

