package fs.client.ui.event;

import fs.client.ui.Component;

public class TextChanged extends UIEvent {
  private final String text;

  public TextChanged(Component target, String text) {
    super(target);
    this.text = text;
  }

  public String text() {
    return text;
  }
}
