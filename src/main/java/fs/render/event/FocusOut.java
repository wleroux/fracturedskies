package fs.render.event;

import fs.render.Component;

public class FocusOut extends UIEvent {
  public FocusOut(Component target) {
    super(target);
  }
}
