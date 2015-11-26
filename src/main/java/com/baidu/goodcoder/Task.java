package com.baidu.goodcoder;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by miaohong01 on 15/10/22.
 */

public class Task implements Delayed {
    private String host;
    private String ip;
    private String id;
    private long taskInsertTime;
    private long taskExpiredTime;

    public Task( String id, String host, String ip) {
        this.id = id;
        this.host = host;
        this.ip = ip;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getTaskExpiredTime() {
        return taskExpiredTime;
    }

    public void setTaskExpiredTime(long taskExpiredTime) {
        this.taskExpiredTime = taskExpiredTime;
    }

    public long getTaskInsertTime() {
        return taskInsertTime;
    }

    public void setTaskInsertTime(long taskInsertTime) {
        this.taskInsertTime = taskInsertTime;
    }

    public boolean isValid() {
        return this.id != null && this.host != null && this.ip != null;
    }

    public boolean isExpired() {
        return taskExpiredTime > 0 && taskInsertTime + taskExpiredTime < System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Task [id=" + id + ", host=" + host + ", ip=" + ip + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task) {
            Task task = (Task) obj;
            if (id == null) {
                if (task.getId() == null) {
                    return true;
                } else {
                    return false;
                }
            }

            return id.equals(task.getId());

        }
        return false;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long now = System.currentTimeMillis();
        return unit.convert(taskExpiredTime - (now - taskInsertTime), TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed o) {
        if (o == this) {
            return 0;
        }

        long diff = getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);

        if (diff == 0) {
            return 0;
        } else if (diff < 0) {
            return -1;
        } else {
            return 1;
        }
    }
}
