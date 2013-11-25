package util;

public class PointF {
  public double x;
  public double y;

  public PointF() {
  }

  public PointF(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public PointF(PointF size) {
    this.x = size.x;
    this.y = size.y;
  }

}
