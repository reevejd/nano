package com.airhacks.nano;

import java.nio.file.Path;
import java.nio.file.Paths;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author airhacks.com
 */
public class ContextsTest {

    /*
     * A test for Contexts.extractContext(). 
     * Asserts that the leading ".", and file extension are removed,
     * leaving a proper http context.
     */

    @Test
    public void extractContext() {
        Path path = Paths.get("./src/test/js/hello/duke.js");
        String actual = Contexts.extractContext(path);
        Path actualPath = Paths.get(actual);
        Path expectedPath = Paths.get("/src/test/js/hello/duke");
        assertThat(actualPath, is(expectedPath));
    }

}
