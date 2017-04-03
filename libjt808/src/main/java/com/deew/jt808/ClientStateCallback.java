package com.deew.jt808;

import com.deew.jt808.msg.RegisterReply;
import com.deew.jt808.msg.ServerGenericReply;

/**
 * @author DeeW   (Find me on ---> https://github.com/D-3)
 * @time 2017/4/3
 */

public interface ClientStateCallback {
    /**
     * callback when client connect to server
     */
    void connectSuccess();

    /**
     * callback when client connect to server
     */
    void connectFail();

    /**
     * callback when connection between client and server closed
     */
    void connectionClosed();

    /**
     * callback when client registered on server
     */
    void registerComplete(RegisterReply reply);

    /**
     * callback when client authenticated on server
     */
    void authComplete(ServerGenericReply reply);
}
