package com.zakgof.rxswt;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Display;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.subscriptions.BooleanSubscription;

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

    private final CompositeDisposable workerSubscription = new CompositeDisposable();

    public void dispose() {
      workerSubscription.dispose();
    }

    public boolean isDisposed() {
      return workerSubscription.isDisposed();
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
      final Disposable childSubscription = new BooleanSubscription();
      workerSubscription.add(childSubscription);
      executor.accept(() -> {
        if (!childSubscription.isDisposed() && !workerSubscription.isDisposed()) {
          action.run();
        }
        workerSubscription.remove(childSubscription);
      });
      return BooleanSubscription.create(() -> {
        childSubscription.unsubscribe();
        workerSubscription.remove(childSubscription);
      });
    }

  }

}
