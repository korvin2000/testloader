package de.microtools.cs.lol.loader.application.ftp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;
import java.security.Security;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * The SFTP Implementation of the IFileTransferClient.
 * See <a href="https://github.com/mwiede/jsch/tree/master/examples">Github</a>
 * for more examples if you need to enhance this part.
 */
public class SFTPClient implements IFileTransferClient {

    private static final Logger logger = LoggerFactory.getLogger(SFTPClient.class);
    private Session session;
    private Channel channel;
    private ChannelSftp channelSftp;

    @Override
    public void connect(FileTransferConfig config) throws Exception {
        logger.info("Connecting to SFTP server, to host: " + config.getHost() + " and port " + config.getPort() + " and user " + config.getUsername());
        // Add the Bouncy Castle provider
        Security.addProvider(new BouncyCastleProvider());
        if (Security.getProvider("BC") == null){
            logger.warn("Bouncy Castle provider is NOT available");
        }
        else{
            logger.debug("Bouncy Castle provider is available");
        }
        JSch jsch = new JSch();
        if (StringUtils.isNotEmpty(config.getKnownHosts())) {
            jsch.setKnownHosts(config.getKnownHosts());
        }
        setSftpConfig(config, jsch);
        session.connect();
        if (session.isConnected() == true) {
            logger.debug("Connection to Session server is successfully");
        } else {
            logger.warn("Connection to Session server is NOT successfully");
        }
        channel = session.openChannel("sftp");
        channel.connect();
        channelSftp = (ChannelSftp) channel;
    }

    private void setSftpConfig(FileTransferConfig config, JSch jsch) throws JSchException {
        session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
        if (StringUtils.isNotEmpty(config.getStrictHostKeyChecking())) {
            session.setConfig("StrictHostKeyChecking", config.getStrictHostKeyChecking());
        }
        if (StringUtils.isNotEmpty(config.getServerHostKey())) {
            session.setConfig("server_host_key", config.getServerHostKey());
        }
        if (StringUtils.isNotEmpty(config.getKex())) {
            session.setConfig("kex", config.getKex());
        }
        if (StringUtils.isNotEmpty(config.getCipherS2C())) {
            session.setConfig("cipher.s2c", config.getCipherS2C());
        }
        if (StringUtils.isNotEmpty(config.getCipherC2S())) {
            session.setConfig("cipher.c2s", config.getCipherC2S());
        }
        if (StringUtils.isNotEmpty(config.getMacS2C())) {
            session.setConfig("mac.s2c", config.getMacS2C());
        }
        if (StringUtils.isNotEmpty(config.getMacC2S())) {
            session.setConfig("mac.c2s", config.getMacC2S());
        }
        if (StringUtils.isNotEmpty(config.getIdentityFullPath())) {
            jsch.addIdentity(config.getIdentityFullPath(), config.getPassword());
        }
        session.setPassword(config.getPassword());
    }

    @Override
    public List<String> listNames(String remotePath) throws Exception {
        Vector<ChannelSftp.LsEntry> lsEntries = channelSftp.ls(remotePath);
        List<String> fileNames = new ArrayList<>();
        for (ChannelSftp.LsEntry entry : lsEntries) {
            fileNames.add(entry.getFilename());
        }
        return fileNames;
    }

    @Override
    public void upload(String remotePath, InputStream input) throws Exception {
        channelSftp.put(input, remotePath);
    }

    @Override
    public InputStream download(String remotePath) throws Exception {
        return channelSftp.get(remotePath);
    }

    @Override
    public void downloadFile(String remotePath, OutputStream output) throws Exception {
        logger.info("Downloading file: " + remotePath);
        channelSftp.get(remotePath, output);
    }

    @Override
    public void delete(String remotePath) throws Exception {
        channelSftp.rm(remotePath);
    }

    @Override
    public void close() {
        channelSftp.disconnect();
    }
}
