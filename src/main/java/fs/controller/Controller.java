package fs.controller;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class Controller {

  private Set<Key> keys = new HashSet<>();

  public enum Key {
    FORWARD,
    BACKWARDS,
    UP,
    DOWN,
    LEFT,
    RIGHT
  }

  public void press(Key key) {
    keys.add(key);
  }

  public void release(Key key) {
    keys.remove(key);
  }

  public boolean isPressed(Key key) {
    return keys.contains(key);
  }
}
