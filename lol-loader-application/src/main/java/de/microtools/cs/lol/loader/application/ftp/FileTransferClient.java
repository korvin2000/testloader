package de.microtools.cs.lol.loader.application.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * FTP / SFTP Facade Client
 */
public class FileTransferClient {

    private IFileTransferClient client;

    public FileTransferClient(FileTransferConfig fileTransferConfig) throws Exception {
        if (fileTransferConfig.isUseSftp()) {
            client = new SFTPClient();
            client.connect(fileTransferConfig);
        } else {
            client = new FTPSimpleClient();
            client.connect(fileTransferConfig);
        }
    }

    public List<String> listNames(String remotePath) throws Exception {
        return client.listNames(remotePath);
    }

    public void upload(String remotePath, InputStream input) throws Exception {
        client.upload(remotePath, input);
    }

    public InputStream download(String remotePath) throws Exception {
        return client.download(remotePath);
    }

    public void downloadFile(String remotePath, OutputStream output) throws Exception {
        client.downloadFile(remotePath, output);
    }

    public void delete(String remotePath) throws Exception {
        client.delete(remotePath);
    }

    public void close() throws IOException {
        client.close();
    }
}
