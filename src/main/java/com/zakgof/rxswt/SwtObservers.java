package com.zakgof.rxswt;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import rx.Observable;

public class SwtObservers {
  
  public static Observable<Event> fromListener(Control control, int eventType) {
    Observable<Event> observable = Observable.create(subscriber -> control.addListener(eventType, event -> subscriber.onNext(event)));
    return observable.subscribeOn(SwtScheduler.getInstance());
  }
  
  public static Observable<ControlEvent> fromControlListener(Control control, int eventType) {
    Observable<ControlEvent> observable = Observable.create(subscriber -> control.addControlListener(new ControlListener() {
      
      @Override
      public void controlResized(ControlEvent e) {
        subscriber.onNext(e);
        
      }
      
      @Override
      public void controlMoved(ControlEvent e) {
        // TODO Auto-generated method stub
        
      }
    }));
                                                                                                     
    return observable.subscribeOn(SwtScheduler.getInstance());
  }
  
  public static Observable<MouseEvent> fromMouseListener(Control control) {
    Observable<ControlEvent> observable = Observable.create(subscriber -> control.addMouseListener(new MouseListener() {
      
      @Override
      public void mouseUp(MouseEvent e) {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void mouseDown(MouseEvent e) {
        // TODO Auto-generated method stub
        
      }
      
      @Override
      public void mouseDoubleClick(MouseEvent e) {
        // TODO Auto-generated method stub
        
      }
    });
                                                                                                     
    return observable.subscribeOn(SwtScheduler.getInstance());
  }

}
