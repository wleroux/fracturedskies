package fs.client.ui.event;

import fs.client.ui.Component;

public class FocusOut extends UIEvent {
  public FocusOut(Component target) {
    super(target);
  }
}
