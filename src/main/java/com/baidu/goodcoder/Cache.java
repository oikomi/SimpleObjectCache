package com.baidu.goodcoder;

import java.util.List;

/**
 * A memory cache for task.
 *
 */
public interface Cache {
    /**
     * Add a task into Cache
     *
     * @param task
     *            The task to add in Cache
     * @param expiredTime
     *            The Task expired time .
     * @return true : Add task successfully. false: Add task failed
     */
    public boolean add(Task task, long expiredTime);

    /**
     * Add a list of task into Cache
     *
     * @param tasks
     *            the tasklist
     * @param expiredTime
     *            The Task expired time .
     * @return true : Add task successfully. false: Add task failed
     */
    public boolean add(List<Task> tasks, long expiredTime);

    /**
     * Query an task in Cache
     *
     * @param id
     *            The query id for task
     * @return The task of the matched param id, or null if there is no matched
     *         task
     */
    public Task queryById(String id);

    /**
     * Query all task matched by the given host
     *
     * @param host
     * @return All the matched task in Cache
     */
    public List<Task> queryByHost(String host);

    /**
     * Query all task matched by the given ip
     *
     * @param ip
     * @return All the matched task in Cache
     */
    public List<Task> queryByIp(String ip);
}