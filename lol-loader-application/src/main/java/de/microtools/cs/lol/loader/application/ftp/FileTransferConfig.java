package de.microtools.cs.lol.loader.application.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FileTransferConfig {

    private String username;

    private String host;

    private String password;

    private int port = FTP.DEFAULT_PORT;
    
    private int bufferSize = 2048; //see https://issues.apache.org/jira/browse/NET-207

    private int clientMode = FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE;

    private int fileType = FTP.BINARY_FILE_TYPE;

    private String controlEncoding = FTP.DEFAULT_CONTROL_ENCODING;

    // SFTP only params

    private boolean useSftp = false;

    private String strictHostKeyChecking = "no";
    private String knownHosts = "";
    private String identityFullPath = ""; //For example ~/.ssh/id_dsa
    private String serverHostKey = "ssh-rsa";
    private String kex = "diffie-hellman-group14-sha1";
    private String cipherS2C = "aes128-ctr";
    private String cipherC2S = "aes128-ctr";
    private String macS2C = "hmac-sha1";
    private String macC2S = "hmac-sha1";

    // -----

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getClientMode() {
        return clientMode;
    }

    public void setClientMode(int clientMode) {
        this.clientMode = clientMode;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public boolean isUseSftp() {
        return useSftp;
    }

    public void setUseSftp(boolean useSftp) {
        this.useSftp = useSftp;
    }

    public String getControlEncoding() {
        return controlEncoding;
    }

    public void setControlEncoding(String controlEncoding) {
        this.controlEncoding = controlEncoding;
    }

    public String getStrictHostKeyChecking() {
        return strictHostKeyChecking;
    }

    public void setStrictHostKeyChecking(String strictHostKeyChecking) {
        this.strictHostKeyChecking = strictHostKeyChecking;
    }

    public String getKnownHosts() {
        return knownHosts;
    }

    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    public String getIdentityFullPath() {
        return identityFullPath;
    }

    public void setIdentityFullPath(String identityFullPath) {
        this.identityFullPath = identityFullPath;
    }

    public String getServerHostKey() {
        return serverHostKey;
    }

    public void setServerHostKey(String serverHostKey) {
        this.serverHostKey = serverHostKey;
    }

    public String getKex() {
        return kex;
    }

    public void setKex(String kex) {
        this.kex = kex;
    }

    public String getCipherS2C() {
        return cipherS2C;
    }

    public void setCipherS2C(String cipherS2C) {
        this.cipherS2C = cipherS2C;
    }

    public String getCipherC2S() {
        return cipherC2S;
    }

    public void setCipherC2S(String cipherC2S) {
        this.cipherC2S = cipherC2S;
    }

    public String getMacS2C() {
        return macS2C;
    }

    public void setMacS2C(String macS2C) {
        this.macS2C = macS2C;
    }

    public String getMacC2S() {
        return macC2S;
    }

    public void setMacC2S(String macC2S) {
        this.macC2S = macC2S;
    }
}
