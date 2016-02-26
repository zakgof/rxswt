package com.zakgof.rxswt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class MenuBuilder {
  
  private Menu menu;
  private MenuBuilder parent;
  
  public MenuBuilder(Shell shell) {
    this.menu = new Menu(shell, SWT.BAR);
  }

  private MenuBuilder(Menu menu, MenuBuilder parent) {
    this.menu = menu;
    this.parent = parent;
  }

  public MenuBuilder menu(String name) {
    MenuItem item = new MenuItem(menu, SWT.CASCADE);
    item.setText(name);
    Menu childMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
    item.setMenu(childMenu);
    MenuBuilder builder = new MenuBuilder(childMenu, this);
    return builder;
  }

  public MenuBuilder item(String name, Runnable action) {
    MenuItem child = new MenuItem(menu, SWT.PUSH);
    child.setText(name);
    SwtObservers.fromSelectionListener(child).subscribe(e -> action.run());
    return this;
  }

  public MenuBuilder separator() {
    new MenuItem(menu, SWT.SEPARATOR);
    return this;
  }

  public MenuBuilder done() {
    return parent;
  }

  public Menu build() {
    return menu;
  }
  
  

}
