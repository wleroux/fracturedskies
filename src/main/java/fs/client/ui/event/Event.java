package fs.client.ui.event;

import fs.client.ui.Component;

public abstract class Event {

    private final Component target;

    private boolean propagationStopped = false;

    protected Event(Component target) {
        this.target = target;
    }

    public final Component target() {
        return target;
    }

    public final void stopPropagation() {
        propagationStopped = true;
    }

    public final boolean isPropagationStopped() {
        return propagationStopped;
    }

    /** Event Handling */
    public static void dispatch(Event event) {
        Component ancestor = event.target();
        while (ancestor != null) {
            ancestor.handle(event);
            if (event.isPropagationStopped())
                break;
            else ancestor = ancestor.parent();
        }
    }
}
