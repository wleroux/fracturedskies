package fs.client.ui;

public abstract class Component {

    public abstract int preferredWidth();
    public abstract int preferredHeight();

    public abstract void render(int xOffset, int yOffset, int width, int height);
}
