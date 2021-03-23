package com.del.flc.utils;

import java.util.concurrent.*;

public class MyExecutors {

    private static MyExecutors instance;

    private ExecutorService service;
    private ExecutorService demons;

    private MyExecutors() {
        service = Executors.newCachedThreadPool();
        demons = Executors.newCachedThreadPool(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public static MyExecutors getInstance() {
        if (instance == null) instance = new MyExecutors();
        return instance;
    }


    public Future<?> submit(Runnable cb) {
        return service.submit(cb);
    }

    public void shutdown() {
        service.shutdown();
    }

    public <T> T safeGet(int timeout, TimeUnit unit, Callable<T> cb) {
        Future<T> submit = service.submit(() -> {
            Future<T> demon = demons.submit(cb);
            try {
                return demon.get(timeout, unit);
            } catch (Exception e) {
                return null;
            }
        });
        try {
            return submit.get();
        } catch (Exception e) {
            return null;
        } finally {
            submit.cancel(true);
        }
    }

    public void safeSubmit(int timeout, TimeUnit unit, Runnable cb) {
        Future<?> submit = service.submit(() -> {
            Future<?> demon = demons.submit(cb);
            try {
                return demon.get(timeout, unit);
            } catch (Exception e) {
                return null;
            }
        });
        try {
            submit.get();
        } catch (Exception e) {
            //
        } finally {
            submit.cancel(true);
        }
    }


}
