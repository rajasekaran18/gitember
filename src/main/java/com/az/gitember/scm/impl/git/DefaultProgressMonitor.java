package com.az.gitember.scm.impl.git;

import org.eclipse.jgit.lib.ProgressMonitor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Igor_Azarny on 03 -Jan - 2017.
 */
public class DefaultProgressMonitor implements ProgressMonitor {

    private final BiConsumer<String, Double> percentageCompleteConsumer;

    private int totalWork = Integer.MAX_VALUE;
    private int completed = 0;
    private String title;

    public DefaultProgressMonitor(BiConsumer<String, Double> percentageCompleteConsumer) {
        this.percentageCompleteConsumer = percentageCompleteConsumer;
    }

    @Override
    public void start(int totalTasks) {
        percentageCompleteConsumer.accept(title, 0.0);

    }

    @Override
    public void beginTask(String title, int totalWork) {
        setTotalWork(totalWork);
        this.completed = 0;
        this.title = title;

    }

    @Override
    public void update(int completed) {
        setCompleted(completed);
    }

    @Override
    public void endTask() {
        percentageCompleteConsumer.accept(title, 1.0);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    public void setTotalWork(int totalWork) {
        this.totalWork = totalWork;
        if (totalWork > 0) {
            percentageCompleteConsumer.accept(title, 1.0 * completed  / totalWork);
        }
    }

    public void setCompleted(int completed) {
        this.completed += completed;
        if (totalWork > 0) {
            percentageCompleteConsumer.accept(title, 1.0 * this.completed / totalWork);
        }
    }
}
