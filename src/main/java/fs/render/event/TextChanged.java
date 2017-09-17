package fs.render.event;

import fs.render.Component;

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
