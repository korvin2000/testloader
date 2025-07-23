package de.microtools.cs.lol.loader.application.ftp;

import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * The FTP Implementation of the IFileTransferClient
 */
public class FTPSimpleClient implements IFileTransferClient {

    private static final Logger logger = LoggerFactory.getLogger(FTPSimpleClient.class);
    private final FTPClient client = new FTPClient();

    @Override
    public void connect(FileTransferConfig config) throws Exception {
        logger.info("Connecting to FTP server, to host: " + config.getHost() + " and port " + config.getPort());

        FTPClientConfig ftpConfig = new FTPClientConfig();
        client.configure(ftpConfig);
        client.connect(config.getHost(), config.getPort());

        // After connection attempt, you should check the reply code to verify
        // success.
        int reply = client.getReplyCode();
        logger.info("Reply code: " + reply);

        if(!FTPReply. isPositiveCompletion(reply)) {
            client.disconnect();
            logger.info("FTP server refused connection.");
            throw new Exception("FTP server refused connection.");
        }

        client.login(config.getUsername(), config.getPassword());
        client.enterLocalActiveMode();
        client.setFileType(config.getFileType());
        client.setBufferSize(config.getBufferSize());
        client.setControlEncoding(config.getControlEncoding());
    }

    @Override
    public List<String> listNames(String remotePath) throws Exception {
        return Arrays.asList(client.listNames(remotePath));
    }

    @Override
    public void upload(String remotePath, InputStream input) throws Exception {
        client.storeFile(remotePath, input);
    }

    @Override
    public InputStream download(String remotePath) throws Exception {
        logger.info("Downloading file: " + remotePath);
        return client.retrieveFileStream(remotePath);
    }

    @Override
    public void downloadFile(String remotePath, OutputStream output) throws Exception {
        logger.info("Downloading file: " + remotePath);
        client.retrieveFile(remotePath, output);
    }

    @Override
    public void delete(String remotePath) throws Exception {
        client.deleteFile(remotePath);
    }

    @Override
    public void close() throws IOException {
        if(client.isConnected()) {
            try {
                client.disconnect();
            } catch(IOException ioe) {
                // do nothing
            }
        }
    }
}
