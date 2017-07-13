package fs.math;

public class Hash {
  public static int hash(int... numbers) {
    int hash = 0;
    for (int number : numbers) {
      hash = hash * 31 + number;
    }
    return hash;
  }
}
