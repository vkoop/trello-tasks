package de.vkoop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class MyTaskScheduler {

    @Autowired
    private final TaskScheduler scheduler;

    private final ConcurrentHashMap<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    @Autowired
    public MyTaskScheduler(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public <T extends Runnable & Identifiable> void schedule(T task, String cronExpression) {
        ScheduledFuture<?> future = scheduler.schedule(task, new CronTrigger(cronExpression));
        tasks.put(task.getId(), future);
    }

    public <T extends Runnable & Identifiable> boolean exists(T task) {
        return tasks.containsKey(task.getId());
    }

    public void cancleAll() {
        for (ScheduledFuture<?> f : tasks.values()) {
            f.cancel(false);
        }
    }


}
