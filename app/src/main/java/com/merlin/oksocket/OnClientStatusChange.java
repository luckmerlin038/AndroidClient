package com.merlin.oksocket;

import com.merlin.client.Client;

public interface OnClientStatusChange {

    int LOGIN_IN = 123;
    int LOGIN_OUT = 124;

    void onClientStatusChanged(boolean autoNotify,int what,Object data, Client client);
}
