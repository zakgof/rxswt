package com.zakgof.rxswt;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Display;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.BooleanSubscription;
import rx.subscriptions.CompositeSubscription;

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

    private final CompositeSubscription workerSubscription = new CompositeSubscription();

    public void unsubscribe() {
      workerSubscription.unsubscribe();
    }

    public boolean isUnsubscribed() {
      return workerSubscription.isUnsubscribed();
    }

    @Override
    public Subscription schedule(Action0 action) {
      if (Display.getCurrent() != null)
        return schedule(action, Runnable::run);
      return schedule(action, runnable -> Display.getDefault().asyncExec(runnable));
    }

    @Override
    public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
      // TODO : assert millis range
      int millis = (int) unit.toMillis(delayTime);
      return schedule(action, runnable -> Display.getDefault().timerExec(millis, runnable));
    }

    private Subscription schedule(Action0 action, Consumer<Runnable> executor) {
      final Subscription childSubscription = BooleanSubscription.create();
      workerSubscription.add(childSubscription);
      executor.accept(() -> {
        if (!childSubscription.isUnsubscribed() && !workerSubscription.isUnsubscribed()) {
          action.call();
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
