package fs.render.event;

import fs.render.Component;

public class Click extends UIEvent {
  public Click(Component target) {
    super(target);
  }
}
