# rxswt
RxJava binding for SWT

###Use Observables to listen to SWT events:

#####Old style

```java
    tree.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent evt) {
            if (evt.character == SWT.DEL) {
                controller.deleteSelection(evt.stateMask);
            }
        }
    });
```
#####Rxjava style
```java
    SwtObservers.fromKeyListener(tree)
      .filter(evt -> evt.character == SWT.DEL)
      .subscribe(evt -> controller.deleteSelection(evt.stateMask));
```
Note that you can safely subscribe on a non-UI thread, but handler will be executed on the UI thread.

###SwtSheduler 

SwtSheduler is a rx.Scheduler implementation that executes code on SWT UI thread.

```java
SwtScheduler.getInstance()
        .createWorker()
        .schedule(() -> System.err.println("I'm on UI thread"));
```
