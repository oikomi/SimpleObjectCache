package com.baidu.goodcoder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

/**
 * Created by miaohong01 on 15/10/16.
 */

/**
 * CacheManager for manager mem cache
 */
public class CacheManager implements Cache {
    private final ConcurrentHashMap<String, Task> idTaskMap;
    private final ConcurrentHashMap<String, List<Task>> hostTasksMap;
    private final ConcurrentHashMap<String, List<Task>> ipTasksMap;
    private BlockingQueue<Task> delayQueue;
    private static CacheManager cacheManager;

    private static final Logger LOG = LogManager.getLogger(CacheManager.class);
    private final long autoClearIntervalMillis = 10000;

    private CacheManager() {
        idTaskMap = new ConcurrentHashMap<String, Task>();
        hostTasksMap = new ConcurrentHashMap<String, List<Task>>();
        ipTasksMap = new ConcurrentHashMap<String, List<Task>>();
        delayQueue = new DelayQueue<Task>();
        startAutoClearThread();
    }

    private static class SingletonHolder {
        private static final CacheManager INSTANCE = new CacheManager();
    }

    public static final CacheManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * add single task with expire time
     *
     * @param task
     *            :task instance
     * @param expiredTime
     *            :task expire time
     * @return boolean
     *            :true ->add success  false ->add failed
     */
    @Override
    public synchronized boolean add(Task task, long expiredTime) {
        if (task == null || !task.isValid()) {
            return false;
        }

        task.setTaskInsertTime(System.currentTimeMillis());
        task.setTaskExpiredTime(expiredTime);

        Task previousTask = idTaskMap.put(task.getId(), task);

        if (previousTask != null) {
            delTaskInMaps(previousTask);
        }

        addTaskToMaps(task);
        delayQueue.add(task);

        return true;
    }

    /**
     * add muliti tasks with expire time
     *
     * @param tasks
     *            :tasks instances
     * @param expiredTime
     *            :tasks expire time
     * @return boolean
     *            :true ->add success  false ->add failed
     */
    @Override
    public boolean add(List<Task> tasks, long expiredTime) {
        if (tasks == null) {
            return false;
        }

        for (Task task : tasks) {
            if (!add(task, expiredTime)) {
                LOG.error("add " + task.toString() + "Failed");
                return false;
            }
        }

        return true;
    }

    /**
     * query task by task id
     *
     * @param id
     *            :task id
     * @return Task
     *
     */
    @Override
    public Task queryById(String id) {
        Task task = null;

        if (id == null || (task = idTaskMap.get(id)) == null) {
            return null;
        }

        if (task.isExpired()) {
            LOG.error("task : " + task.toString() + "is expired");
            delTaskInMaps(task);
            idTaskMap.remove(task.getId());
            return null;
        }

        return task;
    }

    /**
     * query task by task host
     *
     * @param host
     *            :task host
     * @return List<Task>
     *            : Task list
     *
     */
    @Override
    public List<Task> queryByHost(String host) {
        List<Task> resultList = new ArrayList<Task>();
        List<Task> hostTaskList = hostTasksMap.get(host);

        if (hostTaskList == null) {
            return new ArrayList<Task>();
        }

        for (Task task : hostTaskList) {
            if (task.isExpired()) {
                LOG.error("task : " + task.toString() + "is expired");
                synchronized (this) {
                    if (task.isExpired()) {
                        delTaskInMaps(task);
                        idTaskMap.remove(task.getId());
                    }
                }
            } else {
                resultList.add(task);
            }
        }

        return resultList;
    }

    /**
     * query task by task ip
     *
     * @param ip
     *            :task ip
     * @return List<Task>
     *            : Task list
     *
     */
    @Override
    public List<Task> queryByIp(String ip) {
        List<Task> resultList = new ArrayList<Task>();
        List<Task> ipTaskList = ipTasksMap.get(ip);

        if (ipTaskList == null) {
            return new ArrayList<Task>();
        }

        for (Task task : ipTaskList) {
            if (task.isExpired()) {
                LOG.error("task : " + task.toString() + "is expired");
                synchronized (this) {
                    if (task.isExpired()) {
                        delTaskInMaps(task);
                        idTaskMap.remove(task.getId());
                    }
                }
            } else {
                resultList.add(task);
            }
        }

        return resultList;
    }

    /**
     * delete task
     *
     * @param task
     *            :task
     * @return void
     *
     *
     */
    private void delTaskInMaps(Task task) {
        List<Task> hostTasksList = hostTasksMap.get(task.getHost());

        if (hostTasksList == null) {
            return;
        }

        Iterator hostIter = hostTasksList.iterator();

        while (hostIter.hasNext()) {
            if (hostIter.next().equals(task)) {
                hostIter.remove();
            }
        }

        List<Task> ipTasksList = ipTasksMap.get(task.getHost());

        if (ipTasksList == null) {
            return;
        }

        Iterator ipIter = hostTasksList.iterator();

        while (ipIter.hasNext()) {
            if (ipIter.next().equals(task)) {
                ipIter.remove();
            }
        }
    }

    /**
     * add task to maps
     *
     * @param task
     *            :task
     * @return void
     *
     *
     */
    private void addTaskToMaps(Task task) {
        List<Task> hostTasksList = hostTasksMap.get(task.getHost());

        if (hostTasksList == null) {
            hostTasksList = new ArrayList<Task>();
        }
        hostTasksList.add(task);

        hostTasksMap.put(task.getHost(), hostTasksList);

        List<Task> ipTasksList = ipTasksMap.get(task.getIp());
        if (ipTasksList == null) {
            ipTasksList = new ArrayList<Task>();
        }
        ipTasksList.add(task);
        ipTasksMap.put(task.getIp(), ipTasksList);
    }

    /**
     * clear all expired tasks
     *
     * @param
     *
     * @return void
     *
     *
     */
    private void clearExpiredTasks() {
        List<Task> expiredTasks = new ArrayList<Task>();
        delayQueue.drainTo(expiredTasks);

        for (Task task : expiredTasks) {
            idTaskMap.remove(task.getId());
            hostTasksMap.get(task.getHost()).remove(task);
            ipTasksMap.get(task.getIp()).remove(task);
        }
    }

    /**
     * start a thread to clear all expired tasks
     *
     * @param
     *
     * @return void
     *
     *
     */
    private void startAutoClearThread() {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(autoClearIntervalMillis);
                    } catch (InterruptedException e) {
                        LOG.error(e.getStackTrace());
                    }
                    clearExpiredTasks();
                }
            }
        }).start();
    }
}
