package fs.render.event;

import fs.render.Component;

public abstract class UIEvent {

  private final Component target;

  private boolean propagationStopped = false;

  protected UIEvent(Component target) {
    this.target = target;
  }

  /**
   * UIEvent Handling
   */
  public static void dispatch(UIEvent event) {
    Component ancestor = event.target();
    while (ancestor != null) {
      ancestor.handle(event);
      if (event.isPropagationStopped())
        break;
      else ancestor = ancestor.parent();
    }
  }

  public final Component target() {
    return target;
  }

  public final void stopPropagation() {
    propagationStopped = true;
  }

  public final boolean isPropagationStopped() {
    return propagationStopped;
  }
}
