package fs.client.ui;

import fs.client.ui.event.UIEvent;
import fs.client.ui.primitive.Root;

import java.util.Collections;
import java.util.List;

public abstract class Component {
  protected int x;
  protected int y;
  protected int width;
  protected int height;
  /**
   * Node Tree
   */
  private Component parent;

  public abstract int preferredWidth();

  public abstract int preferredHeight();

  public abstract Component bounds(int x, int y, int width, int height);

  public abstract void render();

  public Component findComponentAt(int x, int y) {
    if (this.x <= x && x <= this.x + this.width) {
      if (this.y <= y && y <= this.y + this.height) {
        for (Component child : children()) {
          Component childComponent = child.findComponentAt(x, y);
          if (childComponent != null) {
            return childComponent;
          }
        }

        return this;
      }
    }

    return null;
  }

  public final Component parent() {
    return parent;
  }

  public final void parent(Component parent) {
    this.parent = parent;
  }

  public List<Component> children() {
    return Collections.emptyList();
  }

  public void handle(UIEvent event) {
    // no-op
  }

  protected Root root() {
    Component ancestor = this;
    while (ancestor != null) {
      if (ancestor instanceof Root) {
        return (Root) ancestor;
      } else {
        ancestor = ancestor.parent();
      }
    }
    return null;
  }

}
