package com.zakgof.rxswt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import rx.Observable;
import rx.subscriptions.Subscriptions;

public class SwtObservers {

  public static Observable<Event> fromListener(Control control, int eventType) {
    Observable<Event> observable = Observable.create(subscriber -> control.addListener(eventType, event -> subscriber.onNext(event)));
    return observable.subscribeOn(SwtScheduler.getInstance());
  }

  public static Observable<DisposeEvent> fromDisposeListener(Widget control) {
    Observable<DisposeEvent> observable = Observable.create(subscriber -> control.addDisposeListener(event -> {
      subscriber.onNext(event);
      subscriber.onCompleted();      
    }));
    return observable.subscribeOn(SwtScheduler.getInstance()).unsubscribeOn(SwtScheduler.getInstance());
  }

  public static Observable<ControlEvent> fromControlListener(Control control, int eventTypes) {
    // TODO: validate eventTypes
    Observable<ControlEvent> observable = Observable.create(subscriber -> {
      ControlListener controlListener = new ControlListener() {
        @Override
        public void controlResized(ControlEvent e) {
          if ((eventTypes & SWT.Resize) != 0) {
            subscriber.onNext(e);
          }
        }

        @Override
        public void controlMoved(ControlEvent e) {
          if ((eventTypes & SWT.Move) != 0) {
            subscriber.onNext(e);
          }
        }
      };
      control.addControlListener(controlListener);
      subscriber.add(Subscriptions.create(() -> control.removeControlListener(controlListener)));
    });
    return wrap(control, observable);
  }

  public static Observable<MouseEvent> fromMouseTrackListener(Control control, int eventTypes) {
    Observable<MouseEvent> observable = Observable.create(subscriber -> {
      MouseTrackListener mouseTrackListener = new MouseTrackListener() {
        @Override
        public void mouseEnter(MouseEvent e) {
          if ((eventTypes & SWT.MouseEnter) != 0) {
            subscriber.onNext(e);
          }
        }

        @Override
        public void mouseExit(MouseEvent e) {
          if ((eventTypes & SWT.MouseExit) != 0) {
            subscriber.onNext(e);
          }
        }

        @Override
        public void mouseHover(MouseEvent e) {
          if ((eventTypes & SWT.MouseHover) != 0) {
            subscriber.onNext(e);
          }
        }
      };
      control.addMouseTrackListener(mouseTrackListener);
      subscriber.add(Subscriptions.create(() -> control.removeMouseTrackListener(mouseTrackListener)));
    });
    return wrap(control, observable);
  }

  public static Observable<MouseEvent> fromMouseListener(Control control, int eventTypes) {
    Observable<MouseEvent> observable = Observable.create(subscriber -> {
      MouseListener mouseListener = new MouseListener() {
        @Override
        public void mouseDoubleClick(MouseEvent e) {
          if ((eventTypes & SWT.MouseDoubleClick) != 0) {
            subscriber.onNext(e);
          }
        }

        @Override
        public void mouseDown(MouseEvent e) {
          if ((eventTypes & SWT.MouseDown) != 0) {
            subscriber.onNext(e);
          }
        }

        @Override
        public void mouseUp(MouseEvent e) {
          if ((eventTypes & SWT.MouseUp) != 0) {
            subscriber.onNext(e);
          }
        }
      };
      control.addMouseListener(mouseListener);
      subscriber.add(Subscriptions.create(() -> control.removeMouseListener(mouseListener)));
    });
    return wrap(control, observable);
  }

  public static Observable<ShellEvent> fromShellListener(Shell shell, int eventTypes) {
    // TODO: validate eventTypes
    Observable<ShellEvent> observable = Observable.create(subscriber -> {
      ShellListener shellListener = new ShellListener() {

        @Override
        public void shellActivated(ShellEvent e) {
          if ((eventTypes & SWT.Activate) != 0) {
            subscriber.onNext(e);
          }
        }

        @Override
        public void shellClosed(ShellEvent e) {
          if ((eventTypes & SWT.Close) != 0) {
            subscriber.onNext(e);
          }
        }

        @Override
        public void shellDeactivated(ShellEvent e) {
          if ((eventTypes & SWT.Deactivate) != 0) {
            subscriber.onNext(e);
          }

        }

        @Override
        public void shellDeiconified(ShellEvent e) {
          if ((eventTypes & SWT.Deiconify) != 0) {
            subscriber.onNext(e);
          }
        }

        @Override
        public void shellIconified(ShellEvent e) {
          if ((eventTypes & SWT.Iconify) != 0) {
            subscriber.onNext(e);
          }
        }
      };
      shell.addShellListener(shellListener);
      subscriber.add(Subscriptions.create(() -> shell.removeShellListener(shellListener)));
    });
    return wrap(shell, observable);
  }

  public static Observable<TraverseEvent> fromTraverseListener(Control control) {
    Observable<TraverseEvent> observable = Observable.create(subscriber -> {
      TraverseListener traverseListener = traverseEvent -> {
        // TODO: doit ?
        subscriber.onNext(traverseEvent);
      };
      control.addTraverseListener(traverseListener);
      subscriber.add(Subscriptions.create(() -> control.removeTraverseListener(traverseListener)));
    });
    return wrap(control, observable);
  }
  
  public static Observable<SelectionEvent> fromSelectionListener(MenuItem item) {
    Observable<SelectionEvent> observable = Observable.create(subscriber -> {
      SelectionListener selectionListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          subscriber.onNext(e);
        }
      };
    item.addSelectionListener(selectionListener);
    subscriber.add(Subscriptions.create(() -> item.removeSelectionListener(selectionListener)));    
    });
    return wrap(item, observable);
  }

  private static <E> Observable<E> wrap(Widget control, Observable<E> observable) {
    Observable<DisposeEvent> disposeObservable = fromDisposeListener(control);
    return Observable.merge(observable.subscribeOn(SwtScheduler.getInstance()).unsubscribeOn(SwtScheduler.getInstance()), disposeObservable.filter(i -> false).map(i -> null));
  }
  
  public static Observable<String> fromModifyListener(Text text) {
    text.addModifyListener(listener -> text.getText());
    Observable<String> observable = Observable.create(subscriber -> {
      ModifyListener modifyListener = event -> subscriber.onNext(text.getText());
      text.addModifyListener(modifyListener);
      subscriber.add(Subscriptions.create(() -> text.removeModifyListener(modifyListener)));
    });
    return wrap(text, observable);
  }

}
