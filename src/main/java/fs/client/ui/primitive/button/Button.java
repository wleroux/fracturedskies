package fs.client.ui.primitive.button;

import fs.client.ui.Component;
import fs.client.ui.event.*;
import fs.client.ui.layout.Card;
import fs.client.ui.layout.Flex;
import fs.client.ui.primitive.label.Label;
import fs.math.Color4;
import fs.math.Matrix4;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Button extends Component {

    private final Card card = new Card();
    private final Label label;
    private final ButtonBase buttonBase;
    private int topPadding;
    private int rightPadding;
    private int bottomPadding;
    private int leftPadding;
    private EventHandler<Click> clickHandler = (event) -> {};


    public Button(Matrix4 projection) {
        label = new Label(projection)
            .padding(5, 5, 5, 5);
        buttonBase = new ButtonBase(projection);

        Flex labelFlex = new Flex()
                .justifyContent(Flex.JustifyContent.CENTER)
                .alignItems(Flex.ItemAlign.CENTER)
                .alignContent(Flex.ContentAlign.STRETCH)
                .add(label);

        card.add(buttonBase);
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
        buttonBase.color(backgroundColor);

        return this;
    }

    public Button padding(int top, int right, int bottom, int left) {
        this.topPadding = top;
        this.rightPadding = right;
        this.bottomPadding = bottom;
        this.leftPadding = left;

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
    public void handle(Event event) {
        if (event instanceof MouseOver) {
            buttonBase.hover(true);
        } else if (event instanceof MouseOut) {
            buttonBase.hover(false);
        } else if (event instanceof MouseDown) {
            if (((MouseDown) event).button() == GLFW_MOUSE_BUTTON_LEFT) {
                event.stopPropagation();
                Event.dispatch(new Click(this));
            }
        } else if (event instanceof Click) {
            clickHandler.handle((Click) event);
        }
    }

    @Override
    public int preferredWidth() {
        return card.preferredWidth() + leftPadding + rightPadding;
    }

    @Override
    public int preferredHeight() {
        return card.preferredHeight() + topPadding + bottomPadding;
    }

    @Override
    public void render() {
        card
                .bounds(x + leftPadding, y + topPadding, width - leftPadding - rightPadding, height - topPadding - bottomPadding)
                .render();
    }

    public String toString() {
        return "Button[" + label.text() + "]";
    }

    public Button onclick(EventHandler<Click> clickHandler) {
        this.clickHandler = clickHandler;

        return this;
    }
}
