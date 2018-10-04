package util;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache<K, V> extends LinkedHashMap<K, V> {

    public final int maxSize;

    public Cache(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxSize;
    }
}
