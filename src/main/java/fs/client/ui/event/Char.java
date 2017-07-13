package fs.client.ui.event;

import fs.client.ui.Component;

public class Char extends UIEvent {
  private final Character character;
  private final int mods;

  public Char(Component target, Character character, int mods) {
    super(target);

    this.character = character;
    this.mods = mods;
  }

  public Character character() {
    return character;
  }

  public int mods() {
    return mods;
  }
}
