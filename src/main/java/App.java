package quickstart;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import ratpack.server.RatpackServer;
import quickstart.DocVersioner;

public class App {
    public static void main(String... args) throws Exception, IOException, GeneralSecurityException {
        // test server
        RatpackServer.start(server -> server 
         // receives a chain object (for the response handling strategy)
         .handlers(chain -> chain
            .get(ctx -> ctx.render("this World!"))
            .get(":name", ctx -> ctx.render("Hello " + ctx.getPathTokens().get("name") + "!"))     
         )
       );
    }
}
