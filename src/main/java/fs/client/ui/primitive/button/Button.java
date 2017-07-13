package fs.client.ui.primitive.button;

import fs.client.ui.Component;
import fs.client.ui.event.*;
import fs.client.ui.layout.Card;
import fs.client.ui.layout.Flex;
import fs.client.ui.primitive.label.Label;
import fs.client.ui.primitive.mesh.TextureArray;
import fs.math.Color4;
import fs.math.Matrix4;

import static fs.util.ResourceLoader.loadAsByteBuffer;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Button extends Component {

  private final Card card = new Card();
  private final Label label;
  private final Base base;
  private int topMargin;
  private int rightMargin;
  private int bottomMargin;
  private int leftMargin;
  private EventHandler<Click> clickHandler = (event) -> {
  };


  public Button(Matrix4 projection) {
    label = new Label(projection)
        .margin(5, 5, 5, 5);

    ClassLoader classLoader = this.getClass().getClassLoader();
    TextureArray defaultTextureArray = new TextureArray(
        loadAsByteBuffer("fs/client/ui/primitive/button/button_default.png", classLoader),
        4,
        4,
        9
    );
    TextureArray hoverTextureArray = new TextureArray(
        loadAsByteBuffer("fs/client/ui/primitive/button/button_hover.png", classLoader),
        4,
        4,
        9
    );

    base = new Base(projection, defaultTextureArray, hoverTextureArray);

    Flex labelFlex = new Flex()
        .justifyContent(Flex.JustifyContent.CENTER)
        .alignItems(Flex.ItemAlign.CENTER)
        .alignContent(Flex.ContentAlign.STRETCH)
        .add(label);

    card.add(base);
    card.add(labelFlex);
  }

  public Button text(String text) {
    label.text(text);
    return this;
  }

  public Button textColor(Color4 color) {
    label.color(color);
    return this;
  }

  public Button backgroundColor(Color4 backgroundColor) {
    base.color(backgroundColor);

    return this;
  }

  public Button margin(int top, int right, int bottom, int left) {
    this.topMargin = top;
    this.rightMargin = right;
    this.bottomMargin = bottom;
    this.leftMargin = left;

    return this;
  }

  @Override
  public Button bounds(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;

    return this;
  }

  @Override
  public Component findComponentAt(int x, int y) {
    if (this.x <= x && x <= this.x + this.width) {
      if (this.y <= y && y <= this.y + this.height) {
        return this;
      }
    }
    return null;
  }

  @Override
  public void handle(UIEvent event) {
    if (event instanceof MouseOver) {
      base.hover(true);
    } else if (event instanceof MouseOut) {
      base.hover(false);
    } else if (event instanceof MouseDown) {
      if (((MouseDown) event).button() == GLFW_MOUSE_BUTTON_LEFT) {
        event.stopPropagation();
        UIEvent.dispatch(new Click(this));
      }
    } else if (event instanceof Click) {
      clickHandler.handle((Click) event);
    }
  }

  @Override
  public int preferredWidth() {
    return card.preferredWidth() + leftMargin + rightMargin;
  }

  @Override
  public int preferredHeight() {
    return card.preferredHeight() + topMargin + bottomMargin;
  }

  @Override
  public void render() {
    card
        .bounds(x + leftMargin, y + topMargin, width - leftMargin - rightMargin, height - topMargin - bottomMargin)
        .render();
  }

  public String toString() {
    return "Button[" + label.text() + "]";
  }

  public Button onclick(EventHandler<Click> clickHandler) {
    this.clickHandler = clickHandler;

    return this;
  }

  public String text() {
    return label.text();
  }
}
