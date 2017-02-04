package fs.client.ui.primitive.input;

import fs.client.ui.Component;
import fs.client.ui.event.*;
import fs.client.ui.layout.Card;
import fs.client.ui.primitive.button.Base;
import fs.client.ui.primitive.label.Label;
import fs.client.ui.primitive.label.LabelMeshGenerator;
import fs.client.ui.primitive.mesh.TextureArray;
import fs.math.Color4;
import fs.math.Matrix4;

import static fs.util.ResourceLoader.loadAsByteBuffer;
import static org.lwjgl.glfw.GLFW.*;

public class Input extends Component {

    private final Card layout;
    private final Label textField;
    private final Base base;
    private final Label cursor;
    private boolean focused = false;
    private int cursorPosition = 3;
    private EventHandler<TextChanged> textChangedHandler = (event) -> {};

    public Input(Matrix4 projection) {
        this.textField = new Label(projection)
                .margin(5, 5, 5, 5);
        this.cursor = new Label(projection)
                .text("|")
                .margin(5, 5, 5, 5);

        ClassLoader classLoader = this.getClass().getClassLoader();
        TextureArray inputTextureArray = new TextureArray(
                loadAsByteBuffer("fs/client/ui/primitive/input/input_default.png", classLoader),
                4,
                4,
                9
        );

        this.base = new Base(projection, inputTextureArray, inputTextureArray);

        layout = new Card();
        layout.parent(this);
        layout.add(base);
        layout.add(textField);
    }

    @Override
    public int preferredWidth() {
        return layout.preferredWidth();
    }

    @Override
    public int preferredHeight() {
        return layout.preferredHeight();
    }

    @Override
    public Input bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        layout.bounds(x, y, width, height);

        return this;
    }

    @Override
    public void render() {
        layout.render();
        if (focused) {
            int xOffset =
                    LabelMeshGenerator.width(textField.displayText().substring(0, cursorPosition)) -
                    + LabelMeshGenerator.width(cursor.displayText()) / 2;
            cursor
                    .bounds(
                            x + xOffset, y,
                            cursor.preferredWidth(),
                            cursor.preferredHeight()
                    )
                    .render();
        }
    }

    @Override
    public void handle(Event event) {
        if (event instanceof MouseDown) {
            cursorPosition = textField.cursorPositionAt(((MouseDown) event).x(), ((MouseDown) event).y());
            root().focus(this);
            event.stopPropagation();
        } else if (event instanceof FocusIn) {
            focused = true;
        } else if (event instanceof FocusOut) {
            focused = false;
        } else if (event instanceof Key) {
            processKey((Key) event);
        } else if (event instanceof Char) {
            processChar((Char) event);
        } else if (event instanceof TextChanged) {
            textChangedHandler.handle((TextChanged) event);
        }
    }

    private boolean processKey(Key key) {
        if (key.action() != GLFW_RELEASE && key.action() != GLFW_REPEAT)
            return false;

        switch (key.key()) {
            case GLFW_KEY_DELETE: {
                if (cursorPosition < text().length()) {
                    String originalText = text();
                    String newText = originalText.substring(0, cursorPosition)
                            + originalText.substring(cursorPosition + 1);

                    text(newText);
                    return true;
                }
                break;
            }
            case GLFW_KEY_BACKSPACE: {
                if (cursorPosition > 0) {
                    String originalText = text();
                    String newText = originalText.substring(0, cursorPosition - 1)
                            + originalText.substring(cursorPosition);

                    text(newText);
                    cursorPosition -= 1;
                    return true;
                }
                break;
            }
            case GLFW_KEY_RIGHT: {
                cursorPosition += 1;
                break;
            }
            case GLFW_KEY_LEFT: {
                cursorPosition -= 1;
                break;
            }
            default:
                return false;
        }

        cursorPosition = Math.max(0, Math.min(cursorPosition, text().length()));
        return false;
    }

    private boolean processChar(Char c) {
        String originalText = text();
        String newText = originalText.substring(0, cursorPosition)
                + c.character()
                + originalText.substring(cursorPosition);
        text(newText);
        cursorPosition += 1;

        return true;
    }

    public Input onTextChanged(EventHandler<TextChanged> eventHandler) {
        this.textChangedHandler = eventHandler;

        return this;
    }

    public Input backgroundColor(Color4 color) {
        base.color(color);

        return this;
    }

    public Input textColor(Color4 color) {
        textField.color(color);

        return this;
    }

    public Input text(String text) {
        if (!textField.text().equals(text)) {
            textField.text(text);
            Event.dispatch(new TextChanged(this, text));
        }

        return this;
    }

    public String text() {
        return textField.text();
    }
}
