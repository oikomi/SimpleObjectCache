package com.baidu.goodcoder;

import junit.framework.TestCase;


/**
 * Created by miaohong01 on 15/10/16.
 */

public class CacheManagerTest extends TestCase {
    public void testAddTask() {
        CacheManager mCache = CacheManager.getInstance();

        assertNull(mCache.queryById("id-0000"));
        assertNull(mCache.queryById("id-0001"));

        Task task1 = new Task("id-0001", "pc", "pc1");
        Task task2 = new Task("id-0002", "pc", "pc2");

        mCache.add(task1, 1000);
        mCache.add(task2, 1000);

        assertEquals(task1, mCache.queryById("id-0001"));
        assertEquals(task2, mCache.queryById("id-0002"));
    }

    public void testQueryById() throws InterruptedException {
        CacheManager mCache = CacheManager.getInstance();

        Task task1 = new Task("id-0001", "pc", "pc1");
        Task task2 = new Task("id-0002", "pc", "pc2");

        mCache.add(task1, 1000);
        mCache.add(task2, 2000);

        assertNull(mCache.queryById(null));
        assertEquals(task1, mCache.queryById("id-0001"));
        assertEquals(task2, mCache.queryById("id-0002"));
        assertNull(mCache.queryById("id-0003"));

        Thread.sleep(1500);
        assertNull(mCache.queryById("id-0000"));
        assertEquals(null, mCache.queryById("id-0001"));
        assertEquals(task2, mCache.queryById("id-0002"));

        Thread.sleep(1500);
        assertNull(mCache.queryById("id-0000"));
        assertNull(mCache.queryById("id-0001"));
        assertNull(mCache.queryById("id-0002"));

    }

    public void testQueryByHost() throws InterruptedException {
        CacheManager mCache = CacheManager.getInstance();

        Task task1 = new Task("id-0001", "pc", "127.0.0.1");
        Task task2 = new Task("id-0002", "pc", "127.0.0.2");

        mCache.add(task1, 1000);
        mCache.add(task2, 5000);

        assertEquals(2, mCache.queryByHost("pc").size());
        assertEquals(0, mCache.queryByHost("no exists").size());
    }


    public void testQueryByIp() throws InterruptedException {
        CacheManager mCache = CacheManager.getInstance();

        Task task1 = new Task("id-0001", "pc", "127.0.0.1");
        Task task2 = new Task("id-0002", "pc", "127.0.0.2");

        mCache.add(task1, 1000);
        mCache.add(task2, 2000);

        assertEquals(task1, mCache.queryByIp("127.0.0.1").get(0));
        assertEquals(task2, mCache.queryByIp("127.0.0.2").get(0));
        assertTrue(mCache.queryByIp("127.0.0.3").isEmpty());

        Thread.sleep(1500);
        assertTrue(mCache.queryByIp("127.0.0.1").isEmpty());
        assertEquals(task2, mCache.queryByIp("127.0.0.2").get(0));
    }
}
