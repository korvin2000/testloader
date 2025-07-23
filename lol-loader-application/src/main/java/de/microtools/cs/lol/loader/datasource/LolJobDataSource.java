package de.microtools.cs.lol.loader.datasource;

import de.microtools.bobiksystem.logging.Logger;
import de.microtools.bobiksystem.logging.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Extended BasicDataSource. Keep alive the basic data source, even if it seems that it lies lazy around.
 * The Spring Batch framework does not keep the own datasource alive, which leads to connection closed of
 * the datasource for writing the protocol. This implementation is intended to solve the problem.
 */
public class LolJobDataSource extends org.apache.commons.dbcp2.BasicDataSource {
    private static final Logger logger = LoggerFactory.getLogger(LolJobDataSource.class);
    private boolean keepAliveThreadStarted = false;
    private boolean stopKeepAliveThread = false;
    private Connection lastConnection = null;

    @Override
    public Connection getConnection() throws SQLException {

        logger.trace("### get datasource");
        Connection connection = super.getConnection();

        if (connection == null) {
            return null;
        }

        lastConnection = connection;

        if (!keepAliveThreadStarted) {

            logger.info("### keep alive the datasource");
            keepAliveThreadStarted = true;

            (new Thread() {
                public void run() {
                    logger.info("### From inside the KEEP ALIVE THREAD, hello!");

                    while (!stopKeepAliveThread) {

                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException e) {
                            logger.error(e);
                            stopKeepAliveThread = false;
                            keepAliveThreadStarted = false;
                            Thread.currentThread().interrupt();
                        }

                        try(Statement stmt = lastConnection.createStatement()) {

                            if (stopKeepAliveThread || lastConnection == null) {
                                break;
                            }

                            if (lastConnection.isClosed()) {
                                logger.warn("### Connection was closed inside the 'keep alive thread', please check if this is ok!");
                                break;
                            }

                            stmt.executeQuery("select 1 from dual");
                            logger.info("###  ... still alive ...");
                        } catch (Exception e) {
                            logger.warn("### Keeping alive the datasource failed ... maybe it was closed in the meantime. No problem, we just leave the keepAliveThread!");
                            logger.debug(e);
                            break;
                        }
                    }

                    stopKeepAliveThread = false;
                    keepAliveThreadStarted = false;
                }
            }).start();
        }

        return connection;
    }

    @Override
    public synchronized void close() throws SQLException {
        super.close();
        logger.info("### Close datasource");
        stopKeepAliveThread = true;
    }
}
