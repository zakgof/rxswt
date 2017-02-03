package com.zakgof.rxswt;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Display;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

/**
 * Scheduler that executes units of work on SWT UI thread.
 */
public class SwtScheduler extends Scheduler {

    @Override
    public Worker createWorker() {
        return new SwtWorker();
    }

    private SwtScheduler() {
    }

    private static final SwtScheduler INSTANCE = new SwtScheduler();

    public static SwtScheduler getInstance() {
        return INSTANCE;
    }

    private static class SwtWorker extends Worker {

        private final CompositeDisposable workerDisposable = new CompositeDisposable();

        public void dispose() {
            workerDisposable.dispose();
        }

        public boolean isDisposed() {
            return workerDisposable.isDisposed();
        }

        @Override
        public Disposable schedule(Runnable action) {
            if (Display.getCurrent() != null)
                return schedule(action, Runnable::run);
            return schedule(action, runnable -> Display.getDefault().asyncExec(runnable));
        }

        @Override
        public Disposable schedule(Runnable action, long delayTime, TimeUnit unit) {
            // TODO : assert millis range
            int millis = (int) unit.toMillis(delayTime);
            return schedule(action, runnable -> Display.getDefault().timerExec(millis, runnable));
        }

        private Disposable schedule(Runnable action, Consumer<Runnable> executor) {
            Disposable unitDisposable = new PrimitiveDisposable();
            workerDisposable.add(unitDisposable);
            executor.accept(() -> {
                if (!unitDisposable.isDisposed() && !workerDisposable.isDisposed()) {
                    action.run();
                }
                workerDisposable.remove(unitDisposable);
            });
            return Disposables.fromAction(() -> {
                unitDisposable.dispose();
                workerDisposable.remove(unitDisposable);
            });
        }

    }

    private static class PrimitiveDisposable implements Disposable {

        private AtomicBoolean disposed = new AtomicBoolean(false);

        @Override
        public void dispose() {
            disposed.set(true);
        }

        @Override
        public boolean isDisposed() {
            return disposed.get();
        }

    }

}
