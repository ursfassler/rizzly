package pir.expression.reference;

abstract public class RefMiddle extends RefItem {
  private RefItem previous;

  public RefMiddle(RefItem previous) {
    super();
    this.previous = previous;
  }

  public RefItem getPrevious() {
    return previous;
  }

  public void setPrevious(RefItem previous) {
    this.previous = previous;
  }

}
