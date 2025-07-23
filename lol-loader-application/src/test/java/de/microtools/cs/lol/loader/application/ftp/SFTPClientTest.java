package de.microtools.cs.lol.loader.application.ftp;

import org.junit.Ignore;
import org.junit.Test;

public class SFTPClientTest {

  @Ignore
  @Test
  public void connect() throws Exception {

    FileTransferConfig config = new FileTransferConfig();
    config.setUseSftp(true);
    //config.setHost();
    config.setPort(22);
    config.setStrictHostKeyChecking("no");
    SFTPClient sftpClient = new SFTPClient();
    sftpClient.connect(config);
    sftpClient.close();
  }

}
