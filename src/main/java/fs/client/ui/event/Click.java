package fs.client.ui.event;

import fs.client.ui.Component;

public class Click extends UIEvent {
  public Click(Component target) {
    super(target);
  }
}
