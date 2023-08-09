import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiringCache<K, V> {
    private final Map<K, V> cache = new HashMap<>();
    private final Map<K, Long> expirationTimes = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ExpiringCache() {
        // Inicializa o executor para limpar os valores expirados
        scheduler.scheduleAtFixedRate(this::cleanupExpiredValues, 0, 1, TimeUnit.SECONDS);
    }

    public void put(K key, V value, long expirationTimeInSeconds) {
        cache.put(key, value);
        long expirationTime = System.currentTimeMillis() + (expirationTimeInSeconds * 1000);
        expirationTimes.put(key, expirationTime);
    }

    public V get(K key) {
        if (isExpired(key)) {
            remove(key);
            return null;
        }
        return cache.get(key);
    }

    public void remove(K key) {
        cache.remove(key);
        expirationTimes.remove(key);
    }

    private boolean isExpired(K key) {
        Long expirationTime = expirationTimes.get(key);
        return expirationTime != null && expirationTime < System.currentTimeMillis();
    }

    private void cleanupExpiredValues() {
        long currentTime = System.currentTimeMillis();
        for (K key : expirationTimes.keySet()) {
            if (expirationTimes.get(key) < currentTime) {
                cache.remove(key);
                expirationTimes.remove(key);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public static void main(String[] args) {
        ExpiringCache<String, Integer> cache = new ExpiringCache<>();
        cache.put("key1", 100, 5); // Valor expira após 5 segundos

        System.out.println(cache.get("key1")); // Deve imprimir 100
        try {
            Thread.sleep(6000); // Aguarda 6 segundos para que o valor expire
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(cache.get("key1")); // Deve imprimir null, pois o valor expirou
        cache.shutdown();
    }
}
