package fs.client.ui.layout;

import fs.client.ui.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class Flex extends Component {

  private final List<Component> children = new ArrayList<>();
  private Direction direction = Direction.ROW;
  private JustifyContent justifyContent = JustifyContent.LEFT;
  private ItemAlign alignItems = ItemAlign.START;
  private ContentAlign alignContent = ContentAlign.START;
  private Wrap wrap = Wrap.NO_WRAP;

  public Flex justifyContent(JustifyContent justifyContent) {
    this.justifyContent = justifyContent;

    return this;
  }

  public Flex alignItems(ItemAlign align) {
    alignItems = align;

    return this;
  }

  public Flex direction(Direction direction) {
    this.direction = direction;

    return this;
  }

  public Flex wrap(Wrap wrap) {
    this.wrap = wrap;

    return this;
  }

  public Flex alignContent(ContentAlign align) {
    alignContent = align;

    return this;
  }

  @Override
  public int preferredWidth() {
    int preferredWidth = 0;
    for (Component component : children) {
      preferredWidth += component.preferredWidth();
    }
    return preferredWidth;
  }

  @Override
  public int preferredHeight() {
    int preferredHeight = 0;
    for (Component component : children) {
      preferredHeight += component.preferredHeight();
    }
    return preferredHeight;
  }

  public Flex bounds(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;

    return this;
  }

  @Override
  public void render() {
    int mainAxisSize = direction.main(width, height);
    List<List<Component>> componentRows = wrap.split(children, direction, width, height);

    int usedCrossSpace = 0;
    for (List<Component> componentRow : componentRows) {
      int rowCrossSpace = 0;
      for (Component component : componentRow) {
        int componentCrossSpace = direction.cross(component.preferredWidth(), component.preferredHeight());
        if (rowCrossSpace < componentCrossSpace) {
          rowCrossSpace = componentCrossSpace;
        }
      }
      usedCrossSpace += rowCrossSpace;
    }
    int crossAxisSpace = direction.cross(width, height);
    int extraCrossSpace = crossAxisSpace - usedCrossSpace;

    int componentCrossOffset = 0;
    int betweenCrossOffset = 0;
    int additionalCrossSpace = 0;
    if (alignContent == ContentAlign.START) {
      componentCrossOffset = 0;
      betweenCrossOffset = 0;
      additionalCrossSpace = 0;
    } else if (alignContent == ContentAlign.END) {
      componentCrossOffset = extraCrossSpace;
      betweenCrossOffset = 0;
      additionalCrossSpace = 0;
    } else if (alignContent == ContentAlign.CENTER) {
      componentCrossOffset = extraCrossSpace / 2;
      betweenCrossOffset = 0;
      additionalCrossSpace = 0;
    } else if (alignContent == ContentAlign.SPACE_BETWEEN) {
      componentCrossOffset = 0;
      betweenCrossOffset = componentRows.size() <= 1 ? 0 : extraCrossSpace / (componentRows.size() - 1);
      additionalCrossSpace = 0;
    } else if (alignContent == ContentAlign.SPACE_AROUND) {
      betweenCrossOffset = extraCrossSpace / componentRows.size();
      componentCrossOffset = betweenCrossOffset / 2;
      additionalCrossSpace = 0;
    } else if (alignContent == ContentAlign.STRETCH) {
      componentCrossOffset = 0;
      betweenCrossOffset = 0;
      additionalCrossSpace = extraCrossSpace / componentRows.size();
    }

    for (List<Component> componentRow : componentRows) {
      int rowMainSpace = 0;
      int rowCrossSpace = 0;
      for (Component component : componentRow) {
        rowMainSpace += direction.main(component.preferredWidth(), component.preferredHeight());
        int componentCrossSpace = direction.cross(component.preferredWidth(), component.preferredHeight());
        if (rowCrossSpace < componentCrossSpace) {
          rowCrossSpace = componentCrossSpace;
        }
      }
      rowCrossSpace += additionalCrossSpace;

      int extraMainSpace = mainAxisSize < rowMainSpace ? 0 : mainAxisSize - rowMainSpace;
      float mainAxisCoefficient = (rowMainSpace < mainAxisSize) ? 1 : (float) mainAxisSize / (float) rowMainSpace;

      int initialMainOffset = 0;
      int betweenMainOffset = 0;
      if (justifyContent == JustifyContent.LEFT) {
        initialMainOffset = 0;
        betweenMainOffset = 0;
      } else if (justifyContent == JustifyContent.RIGHT) {
        initialMainOffset = extraMainSpace;
        betweenMainOffset = 0;
      } else if (justifyContent == JustifyContent.CENTER) {
        initialMainOffset = extraMainSpace / 2;
        betweenMainOffset = 0;
      } else if (justifyContent == JustifyContent.SPACE_BETWEEN) {
        initialMainOffset = 0;
        betweenMainOffset = componentRow.size() <= 1 ? 0 : extraMainSpace / (componentRow.size() - 1);
      } else if (justifyContent == JustifyContent.SPACE_AROUND) {
        betweenMainOffset = extraMainSpace / componentRow.size();
        initialMainOffset = betweenMainOffset / 2;
      }

      int componentMainOffset = initialMainOffset;
      int maxCrossSpace = 0;
      Iterator<Component> componentIterator = direction.iterator(componentRow);
      while (componentIterator.hasNext()) {
        Component component = componentIterator.next();
        int componentMainSpace = (int) (direction.main(component.preferredWidth(), component.preferredHeight()) * mainAxisCoefficient);
        int componentCrossSpace = direction.cross(component.preferredWidth(), component.preferredHeight());

        int initialCrossOffset = 0;
        int cross = 0;
        if (alignItems == ItemAlign.START) {
          cross = componentCrossSpace;
          initialCrossOffset = 0;
        } else if (alignItems == ItemAlign.END) {
          cross = componentCrossSpace;
          initialCrossOffset = rowCrossSpace - cross;
        } else if (alignItems == ItemAlign.CENTER) {
          cross = componentCrossSpace;
          initialCrossOffset = (rowCrossSpace - cross) / 2;
        } else if (alignItems == ItemAlign.STRETCH) {
          cross = rowCrossSpace;
          initialCrossOffset = 0;
        }

        component.bounds(
            x + direction.x(componentMainOffset, componentCrossOffset + initialCrossOffset),
            y + direction.y(componentMainOffset, componentCrossOffset + initialCrossOffset),
            direction.x(componentMainSpace, cross),
            direction.y(componentMainSpace, cross)
        ).render();

        componentMainOffset += componentMainSpace + betweenMainOffset;
        if (maxCrossSpace < cross) {
          maxCrossSpace = cross;
        }
      }

      componentCrossOffset += maxCrossSpace + betweenCrossOffset;
    }
  }

  @Override
  public Component findComponentAt(int x, int y) {
    if (this.x <= x && x <= this.x + this.width) {
      if (this.y <= y && y <= this.y + this.height) {
        for (Component child : children()) {
          Component childComponent = child.findComponentAt(x, y);
          if (childComponent != null) {
            return childComponent;
          }
        }
      }
    }

    return null;
  }

  @Override
  public List<Component> children() {
    return children;
  }

  public Flex add(Component child) {
    child.parent(this);
    children.add(child);

    return this;
  }

  public enum Direction {
    ROW {
      public int main(int x, int y) {
        return x;
      }

      public int cross(int x, int y) {
        return y;
      }

      @Override
      public int x(int main, int cross) {
        return main;
      }

      @Override
      public int y(int main, int cross) {
        return cross;
      }

      @Override
      public Iterator<Component> iterator(List<Component> componentRow) {
        return componentRow.iterator();
      }
    },
    ROW_REVERSE {
      public int main(int x, int y) {
        return x;
      }

      public int cross(int x, int y) {
        return y;
      }

      @Override
      public int x(int main, int cross) {
        return main;
      }

      @Override
      public int y(int main, int cross) {
        return cross;
      }

      @Override
      public Iterator<Component> iterator(List<Component> componentRow) {
        List<Component> reverse = new ArrayList<>(componentRow);
        Collections.reverse(reverse);
        return reverse.iterator();
      }
    },
    COLUMN {
      public int main(int x, int y) {
        return y;
      }

      public int cross(int x, int y) {
        return x;
      }

      @Override
      public int x(int main, int cross) {
        return cross;
      }

      @Override
      public int y(int main, int cross) {
        return main;
      }

      @Override
      public Iterator<Component> iterator(List<Component> componentRow) {
        return componentRow.iterator();
      }
    },
    COLUMN_RESVERSE {
      public int main(int x, int y) {
        return y;
      }

      public int cross(int x, int y) {
        return x;
      }

      @Override
      public int x(int main, int cross) {
        return cross;
      }

      @Override
      public int y(int main, int cross) {
        return main;
      }

      @Override
      public Iterator<Component> iterator(List<Component> componentRow) {
        List<Component> reverse = new ArrayList<>(componentRow);
        Collections.reverse(reverse);
        return reverse.iterator();
      }
    };

    public abstract int main(int x, int y);

    public abstract int cross(int x, int y);

    public abstract int x(int main, int cross);

    public abstract int y(int main, int cross);

    public abstract Iterator<Component> iterator(List<Component> componentRow);
  }

  public enum Wrap {
    WRAP {
      public List<List<Component>> split(List<Component> components, Direction direction, int width, int height) {
        int mainAxisSize = direction.main(width, height);
        List<List<Component>> componentRows = new ArrayList<>();
        int currentComponentIndex = 0;
        while (currentComponentIndex < components.size()) {
          List<Component> componentRow = new ArrayList<>();

          int cumulativeMainAxisSize = 0;
          while (currentComponentIndex < components.size()) {
            Component component = components.get(currentComponentIndex);
            int componentMainSize = direction.main(component.preferredWidth(), component.preferredHeight());
            if (cumulativeMainAxisSize + componentMainSize > mainAxisSize) {
              break;
            } else {
              cumulativeMainAxisSize += componentMainSize;
              componentRow.add(component);
              currentComponentIndex++;
            }
          }
          componentRows.add(componentRow);
        }

        return componentRows;
      }
    },
    NO_WRAP {
      public List<List<Component>> split(List<Component> components, Direction direction, int width, int height) {
        List<List<Component>> componentRows = new ArrayList<>();
        componentRows.add(components);
        return componentRows;
      }
    };

    public abstract List<List<Component>> split(List<Component> components, Direction direction, int width, int height);
  }

  public enum JustifyContent {
    LEFT,
    CENTER,
    RIGHT,
    SPACE_BETWEEN,
    SPACE_AROUND
  }

  public enum ItemAlign {
    START,
    END,
    CENTER,
    STRETCH
  }


  public enum ContentAlign {
    START,
    END,
    CENTER,
    STRETCH,
    SPACE_BETWEEN,
    SPACE_AROUND
  }
}
