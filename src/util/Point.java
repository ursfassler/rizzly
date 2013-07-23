package util;

public class Point {
  public int x;
  public int y;

  public Point() {
  }

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Point(Point size) {
    this.x = size.x;
    this.y = size.y;
  }

}
