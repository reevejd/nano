package com.airhacks.nano;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author airhacks.com
 */
public interface Contexts {

    /*
     * This interface defines a series of methods that are used to discover and define http contexts.
     */

    public static List<Path> discoverContexts(Path root) {
        /* 
         * Given a root path, this method returns a list of paths that contain
         * .js files (which assumed to be http handlers)
         */

        List<Path> jars = new ArrayList<>();

        SimpleFileVisitor visitor = new SimpleFileVisitor<Path>() { // This visitor will visit all files in a file tree

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                /*
                 * The default behaviour of visitFile has been modified so that
                 * any JavaScript files encountered are added to the "jars" List.
                 */
                if (!attributes.isDirectory()) {
                    if (file.toString().endsWith(".js")) {
                        jars.add(file);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(root, visitor);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return jars; // at this point, jars contains a list of all file paths to JavaScript files within the root
    }

    public static HttpHandler instantiate(Path scriptFile) {
        /*
         * This is a helper method for the HttpContext class below.
         * It takes a path to a JavaScript file,
         */

        
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("javascript"); // A JavaScript ScriptEngine will be used to evaluate JavaScript code

        /* attempt to run the JavaScript script */
        try {
            engine.eval(new FileReader(scriptFile.toFile()));
        } catch (ScriptException | FileNotFoundException ex) {
            throw new IllegalStateException(ex);
        }

        /* casting the ScriptEngine to an Invocable 
         * allows the use Invocable's getInterface method,
         * which maps a Java interface onto a script by matching method names
         */
        Invocable invocable = (Invocable) engine;
        NanoRequest request = invocable.getInterface(NanoRequest.class);

        /* 
         * using Java 8's lambda expressions, return an HTTP handler function
         */
        return (HttpExchange he) -> {
            final OutputStream responseBody = he.getResponseBody();
            StringBuilder builder = new StringBuilder();
            ResponseWriter writer = builder::append; /* store a reference to builder's append method, which can be
            passed to in a request.process call below to write an HTTP response */
    
            /* Get the request method (get, post, etc), body, content, request headers, 
            and a container for the response headers. Then these are passed to the request.process method.  */
            final InputStream requestBody = he.getRequestBody();
            String requestContent;
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(requestBody))) {
                requestContent = buffer.lines().collect(Collectors.joining("\n"));
            }
            Headers requestHeaders = he.getRequestHeaders();
            Headers responseHeaders = he.getResponseHeaders();
            int statusCode = request.process( // calling request's process method will now execute the associated JavaScript process method
                he.getRequestMethod(), 
                requestHeaders, 
                responseHeaders, 
                requestContent, 
                writer
                );
            
            String content = builder.toString(); /* a reference to builder's append method was 
            passed to request.process above, the response was written, and the response can now be retrieved from builder */
            
            
            /* update the response headers and body, then flush the output stream to commit/save what was in the buffer */
            he.sendResponseHeaders(statusCode, content.length());
            responseBody.write(content.getBytes());
            responseBody.flush();
            he.close();
        };
    }

    public static HttpContext create(HttpServer server, Path path) {
        /*
         * creates an http context for a given http server using a path to a JavaScript http handler
         */
        HttpHandler handler = instantiate(path); // create the http handler for this path
        final String extracted = extractContext(path); // get the formatted http context string
        HttpContext context = server.createContext(extracted); // use the context string to create the context
        context.setHandler(handler); // assign the handler for this context
        System.out.println("Context registered: " + context.getPath());
        return context;
    }

    public static String extractContext(Path path) {
        /*
         * Translates a file path into a http context path
         * e.g. developer/java/duke.js --> /developer/java/duke
         */
        String fileName = "/" + path.normalize().toString();
        int lastIndexOf = fileName.lastIndexOf(".");
        return fileName.substring(0, lastIndexOf);
    }

}
