package fs.client.ui.label;

import fs.math.Color4;

public class Label {
    private final Color4 color = Color4.color(1, 1, 1, 1);
    private String text;

    public Label(String text, Color4 color) {
        this.text = text;
        this.color.set(color);
    }

    public String text() {
        return text;
    }

    public Color4 color() {
        return color;
    }
}
