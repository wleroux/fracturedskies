package fs.client.async;

public class RemoveBlockRequested {
    private final int index;

    public RemoveBlockRequested(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
