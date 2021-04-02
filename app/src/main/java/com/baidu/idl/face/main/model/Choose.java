package com.baidu.idl.face.main.model;

public class Choose {
    private User user;
    private long time;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Choose(User user, long time) {
        this.user = user;
        this.time = time;
    }

}
