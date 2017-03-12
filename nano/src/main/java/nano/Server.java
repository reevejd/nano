package nano;

import com.airhacks.nano.Contexts;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author airhacks.com
 */
public class Server {

    public static void main(String[] args) throws IOException {
        
        long start = System.currentTimeMillis();

        /* A string parameter can be passed to the main function to specify a root folder,
        otherwise by default the local directory is the root */
        String rootFolder = args.length >= 1 ? args[0] : "."; 

        /* A port can be specified as the 2nd argument to main, otherwise 4242 is the default port */
        int port = args.length >= 2 ? Integer.parseInt(args[1]) : 4242;

        Path root = Paths.get(rootFolder);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        /* search for valid paths of http handlers, starting at the root */
        List<Path> discovered = Contexts.discoverContexts(root);

        /* for each HTTPhandler path that was found above, create an http context url on the server, and
        instantiate an HTTP handler for that context */
        discovered.stream().forEach(p -> Contexts.create(server, p));

        server.start();
        System.out.println("nano started in: " + (System.currentTimeMillis() - start) + "ms at port: " + port);
    }
}
