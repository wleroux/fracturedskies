package fs.client.ui.event;

import fs.client.ui.Component;

public class MouseDown extends Event {
    private final int button;
    private final int x;
    private final int y;

    public MouseDown(Component target, int button, int x, int y) {
        super(target);

        this.button = button;
        this.x = x;
        this.y = y;
    }

    public int button() {
        return button;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }
}
