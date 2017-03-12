package fs.client.ui.primitive;

import fs.client.ui.Component;
import fs.client.ui.event.*;
import fs.client.ui.layout.Card;

import static org.lwjgl.glfw.GLFW.*;

public class Root extends Card {

    private int screenWidth;
    private int screenHeight;
    private int mouseX;
    private int mouseY;
    private Component mouseover = null;
    private Component focus = null;

    @Override
    public void handle(Event event) {
        if (event instanceof MouseDown) {
            blur();
        }
    }

    public void focus(Component component) {
        if (focus != component) {
            if (focus != null) {
                Event.dispatch(new FocusOut(focus));
                focus = null;
            }

            if (component != null) {
                focus = component;
                Event.dispatch(new FocusIn(focus));
            }
        }
    }

    public void blur() {
        focus(null);
    }

    // GLFW Event Handlers

    public void onWindowSize(long window, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void onKey(long window, int key, int scancode, int action, int mods) {
        if (focus != null) {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                blur();
            } else {
                Event.dispatch(new Key(focus, key, scancode, action, mods));
            }
        } else {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                glfwSetWindowShouldClose(window, true);
            }
        }
    }

    public void onCursorPos(long window, double xpos, double ypos) {
        mouseX = (int) xpos;
        mouseY = screenHeight - (int) ypos;

        Component component = findComponentAt(mouseX, mouseY);
        if (mouseover != component) {
            if (mouseover != null)
                Event.dispatch(new MouseOut(mouseover));
            if (component != null)
                Event.dispatch(new MouseOver(component));
            mouseover = component;
        }

        if (component != null) {
            Event.dispatch(new MouseMove(component, (int) xpos, (int) ypos));
        }
    }

    public void onMouseButton(long window, int button, int action, int mods) {
        if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE) {
            Component component = findComponentAt(mouseX, mouseY);
            Event.dispatch(new MouseDown(component, button, mouseX, mouseY));
        }
    }

    public void onCharMods(long window, int codepoint, int mods) {
        if (focus != null) {
            Event.dispatch(new Char(focus, Character.valueOf((char) codepoint), mods));
        }
    }
}
