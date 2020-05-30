package com.merlin.player;

import java.io.InputStream;

public interface Playable {
    interface CacheReady{
        void onCacheReady(int what,InputStream inputStream);
    }
    boolean open();
    boolean isOpened();
    boolean close();
    boolean cache(CacheReady cacheReady);
    Meta getMeta();
}
