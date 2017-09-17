package fs.block;

/**
 * Created by FracturedSkies on 1/14/2017.
 */
public enum BlockType {
  AIR(-1, -1),
  BLOCK(2, 1);


  private final int top;
  private final int front;
  private final int left;
  private final int back;
  private final int right;
  private final int bottom;

  BlockType() {
    this(0, 0, 0, 0, 0, 0);
  }

  BlockType(int top, int sides) {
    this(top, sides, sides, sides, sides, sides);
  }

  BlockType(int top, int front, int left, int back, int right, int bottom) {
    this.top = top;
    this.front = front;
    this.left = left;
    this.back = back;
    this.right = right;
    this.bottom = bottom;
  }

  public int top() {
    return top;
  }

  public int front() {
    return front;
  }

  public int left() {
    return left;
  }

  public int back() {
    return back;
  }

  public int right() {
    return right;
  }

  public int bottom() {
    return bottom;
  }
}
