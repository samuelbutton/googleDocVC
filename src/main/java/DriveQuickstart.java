package quickstart;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.FileContent;

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
import quickstart.CORSHandler;

public class DriveQuickstart {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
           HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(6000).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws Exception, IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build();

        String fileId = "13eyL9rx1IxoTwztTcDu9XXKVal2NF0ehIInezKOzGLg";

        // build file name
        StringBuilder filename = new StringBuilder("Portland_ME_script_draft_v");
        DateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy");
        Date date = new Date();
        filename.append(dateFormat.format(date));
        filename.append(".docx");
        String fn = filename.toString();

        // pull out file from Google Drive
        FileOutputStream fos = new FileOutputStream(fn);
        service.files().export(fileId, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        .executeMediaAndDownloadTo(fos);

        String folderId = "1_AK0VpJ3rtq5xDVL0jg2ALWlZ8LJ9TWA";
        
        // set up file to be uploaded
        File fileMetadata = new File();
        fileMetadata.setName(fn);
        fileMetadata.setMimeType("application/vnd.google-apps.document");
        fileMetadata.setParents(Collections.singletonList(folderId));

        // pull file
        java.io.File filePath = new java.io.File("/Users/sambutton/Documents/SoftwareEngineering/indepProjects/googleAPI/quickstart/"+fn);
        FileContent mediaContent = new FileContent("text/docx", filePath);
        File file = service.files().create(fileMetadata, mediaContent)
        .setFields("id, parents")
        .execute();
        System.out.println("File ID: " + file.getId());


        // test server
        RatpackServer.start(server -> server 
         // receives a chain object (for the response handling strategy)
         .handlers(chain -> chain
            // handles all incoming requests, passes job to discrete class
           .all(new CORSHandler())
           .get(ctx -> ctx.render("JEEConf 2016"))
           )
       );
    }
}
