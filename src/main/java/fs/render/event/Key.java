package fs.render.event;

import fs.render.Component;

import static java.lang.String.format;

public class Key extends UIEvent {
  private final int key;
  private final int scancode;
  private final int action;
  private final int mods;

  public Key(Component target, int key, int scancode, int action, int mods) {
    super(target);

    this.key = key;
    this.scancode = scancode;
    this.action = action;
    this.mods = mods;
  }

  public int key() {
    return key;
  }

  public int scancode() {
    return scancode;
  }

  public int action() {
    return action;
  }

  public int mods() {
    return mods;
  }

  public String toString() {
    return format("Key[%d; mods: %s]", key, mods);
  }
}
