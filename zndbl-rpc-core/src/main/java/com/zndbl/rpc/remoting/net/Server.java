package com.zndbl.rpc.remoting.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zndbl.rpc.remoting.net.params.BaseCallback;
import com.zndbl.rpc.remoting.provider.ZndblRpcProviderFactory;

/**
 *
 * @author zndbl
 * @Date 2019/4/3
 */
public abstract class Server {

    protected static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private BaseCallback startedCallback;
    private BaseCallback stoppedCallback;

    public void setStartedCallback(BaseCallback startedCallback) {
        this.startedCallback = startedCallback;
    }

    public void setStoppedCallback(BaseCallback stoppedCallback) {
        this.stoppedCallback = stoppedCallback;
    }

    public abstract void start(final ZndblRpcProviderFactory zndblRpcProviderFactory) throws Exception;

    public void onStarted() {
        if (startedCallback != null) {
            try {
                startedCallback.run();
            } catch (Exception e) {
                LOG.error(">>>>>>>>> zndbl-rpc, server statedCallback error. ", e);
            }
        }
    }

    public abstract void stop() throws Exception;

    public void onStoped() {
        if (stoppedCallback != null) {
            try {
                stoppedCallback.run();
            } catch (Exception e) {
                LOG.error(">>>>>>>>> zndbl-rpc, server stoppedCallback error", e);
            }
        }
    }
}