package com.zndbl.rpc.util;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zndbl
 * @Date 2019/4/3
 */
public class NetUtil {

    private static final Logger LOG = LoggerFactory.getLogger(NetUtil.class);

    public static int findAvailablePort(int defaultPort) {
        int portTmp = defaultPort;
        while (portTmp < 65535) {
            if (!isPortUsed(portTmp)) {
                return portTmp;
            } else {
                portTmp++;
            }
        }
        portTmp = defaultPort--;
        while (portTmp > 0) {
            if (!isPortUsed(portTmp)) {
                return portTmp;
            } else {
                portTmp--;
            }
        }
        throw new ZndblRpcException("no available port");
    }

    public static boolean isPortUsed(int port) {
        boolean used = false;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            used = false;
        } catch (IOException e) {
            LOG.info(">>>>>>>>> zndbl-rpc,port{} is in use", port);
            used = true;
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    LOG.info("");
                }
            }
        }
        return used;

    }
}