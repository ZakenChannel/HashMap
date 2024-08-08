import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.sample.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class HashMapTest {

    private HashMap<String, String> map;

    @BeforeEach
    public void setUp() {
        map = new HashMap<>();
    }

    @Test
    public void putAndGet() {
        assertNull(map.put("key", "value"));
        assertEquals("value", map.get("key"));
    }

    @Test
    public void putOverwrite() {
        map.put("key", "value");
        assertEquals("value", map.put("key", "value1"));
        assertEquals("value1", map.get("key"));
    }

    @Test
    public void remove() {
        map.put("key", "value");
        assertEquals("value", map.remove("key"));
        assertNull(map.get("key"));
    }

    @Test
    public void size() {
        assertEquals(0, map.size());
        map.put("key", "one");
        map.put("key2", "two");
        assertEquals(2, map.size());
    }

    @Test
    public void isEmpty() {
        assertTrue(map.isEmpty());
        map.put("key", "value");
        assertFalse(map.isEmpty());
    }

    @Test
    public void clear() {
        map.put("key", "one");
        map.put("key2", "two");
        map.clear();
        assertEquals(0, map.size());
        assertNull(map.get("key"));
        assertNull(map.get("key2"));
    }

    @Test
    public void containsKey() {
        map.put("key", "one");
        assertTrue(map.containsKey("key"));
        assertFalse(map.containsKey("key2"));
    }

    @Test
    public void containsValue() {
        map.put("key", "value");
        assertTrue(map.containsValue("value"));
        assertFalse(map.containsValue("value2"));
    }

    @Test
    public void resize() {
        for (int i = 0; i < 100; i++) {
            map.put(String.valueOf(i), " value" + i);
        }
        assertEquals(100, map.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(" value" + i, map.get(String.valueOf(i)));
        }
    }

    @Test
    public void threadSafety() throws InterruptedException {
        final int threads = 10;
        final int operationsPerThread = 1000;

        Thread[] threadArray = new Thread[threads];

        for (int i = 0; i < threads; i++) {
            threadArray[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    map.put(String.valueOf(j), " value" + j);
                    map.get(String.valueOf(j));
                    map.remove(String.valueOf(j));
                }
            });
            threadArray[i].start();
        }

        for (Thread t : threadArray) {
            t.join();
        }

        assertTrue(map.isEmpty());
    }
}

