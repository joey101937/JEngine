package GameDemo.RTSDemo.MultiplayerTest;

import java.util.HashMap;

public class SynchronizedHashmap<A,B> {
    
    private final HashMap<A,B> internalStorage;

    public SynchronizedHashmap() {
        this.internalStorage = new HashMap<>();
    }
    
    public B get(A key) {
        return internalStorage.get(key);
    }
    
    public synchronized void put(A key, B value) {
        internalStorage.put(key, value);
    }
    
    public synchronized void clear() {
        internalStorage.clear();
    }
    
    public B getOrDefault(A key, B defaultValue) {
        try {
            var result = get(key);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
