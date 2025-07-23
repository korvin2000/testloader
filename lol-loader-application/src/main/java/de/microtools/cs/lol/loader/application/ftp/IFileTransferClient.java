package de.microtools.cs.lol.loader.application.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface for both FTP and SFTP Clients
 */
public interface IFileTransferClient {
    void connect(FileTransferConfig config) throws Exception;
    List<String> listNames(String remotePath) throws Exception;
    void upload(String remotePath, InputStream input) throws Exception;
    InputStream download(String remotePath) throws Exception;
    void downloadFile(String remotePath, OutputStream output) throws Exception;
    void delete(String remotePath) throws Exception;
    void close() throws IOException;
}
