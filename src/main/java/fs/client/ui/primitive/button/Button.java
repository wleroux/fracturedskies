package fs.client.ui.primitive.button;

import fs.client.ui.Component;
import fs.client.ui.layout.Card;
import fs.client.ui.layout.Flex;
import fs.client.ui.primitive.label.Label;
import fs.math.Color4;
import fs.math.Matrix4;

public class Button extends Component {

    private final Card card = new Card();
    private final Label label;
    private final ButtonBase buttonBase;
    private int topPadding;
    private int rightPadding;
    private int bottomPadding;
    private int leftPadding;

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
    public int preferredWidth() {
        return card.preferredWidth() + leftPadding + rightPadding;
    }

    @Override
    public int preferredHeight() {
        return card.preferredHeight() + topPadding + bottomPadding;
    }

    @Override
    public void render(int xOffset, int yOffset, int width, int height) {
        card.render(xOffset + leftPadding, yOffset + topPadding, width - leftPadding - rightPadding, height - topPadding - bottomPadding);
    }
}
