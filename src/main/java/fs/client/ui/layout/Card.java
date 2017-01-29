package fs.client.ui.layout;

import fs.client.ui.Component;

import java.util.ArrayList;
import java.util.List;

public class Card extends Component {
    private final List<Component> components = new ArrayList<>();

    @Override
    public int preferredWidth() {
        int preferredWidth = 0;
        for (Component component: components) {
            if (component.preferredWidth() > preferredWidth) {
                preferredWidth = component.preferredWidth();
            }
        }
        return preferredWidth;
    }

    @Override
    public int preferredHeight() {
        int preferredHeight = 0;
        for (Component component: components) {
            if (component.preferredHeight() > preferredHeight) {
                preferredHeight = component.preferredHeight();
            }
        }
        return preferredHeight;
    }

    @Override
    public void render(int xOffset, int yOffset, int width, int height) {
        for (Component component: components) {
            component.render(xOffset, yOffset, width, height);
        }
    }

    public void add(Component component) {
        components.add(component);
    }

    public void add(int index, Component component) {
        components.add(index, component);
    }
}
