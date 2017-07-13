package fs.client.ui.layout;

import fs.client.ui.Component;
import fs.client.ui.event.UIEvent;

import java.util.ArrayList;
import java.util.List;

public class Card extends Component {
  private final List<Component> children = new ArrayList<>();

  @Override
  public int preferredWidth() {
    int preferredWidth = 0;
    for (Component component : children) {
      if (component.preferredWidth() > preferredWidth) {
        preferredWidth = component.preferredWidth();
      }
    }
    return preferredWidth;
  }

  @Override
  public int preferredHeight() {
    int preferredHeight = 0;
    for (Component component : children) {
      if (component.preferredHeight() > preferredHeight) {
        preferredHeight = component.preferredHeight();
      }
    }
    return preferredHeight;
  }

  @Override
  public Card bounds(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;

    return this;
  }

  @Override
  public void render() {
    for (Component component : children) {
      component
          .bounds(x, y, width, height)
          .render();
    }
  }


  @Override
  public Component findComponentAt(int x, int y) {
    // Reverse order of children as later child render over previous child
    for (int i = children.size() - 1; i >= 0; i--) {
      Component childComponent = children.get(i).findComponentAt(x, y);
      if (childComponent != null) {
        return childComponent;
      }
    }

    return null;
  }

  @Override
  public List<Component> children() {
    return children;
  }

  @Override
  public void handle(UIEvent event) {
  }

  public void add(Component child) {
    child.parent(this);
    children.add(child);
  }
}
