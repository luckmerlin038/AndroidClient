package com.merlin.server;

import com.merlibn.global.Global;
import com.merlibn.global.Protocol;
import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Heartbeat  implements IPulseSendable {
    private String str = "大道";

    @Override
    public byte[] parse() {
        try {
            return new Protocol().generateFrame("test".getBytes("utf-8"),"林强".getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        return bb.array();
        return null;
    }

}
