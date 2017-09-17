package fs.render.event;

import fs.render.Component;

public class MouseMove extends UIEvent {
  private final int x;
  private final int y;

  public MouseMove(Component target, int x, int y) {
    super(target);

    this.x = x;
    this.y = y;
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
  }
}
