package com.merlin.transport;

import java.util.Collection;

public interface TransportBinder extends Callback {
    Collection<? extends Transport> getRunning(int type);
    boolean callback(int status,Callback callback);
    boolean run(int status,Transport transport,String debug);
}