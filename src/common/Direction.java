package common;

public enum Direction {
  in, out;

  public Direction other(){
    switch(this){
    case in: return out;
    case out: return in;
    default: throw new RuntimeException( "Unknown direction: " + this );
    }
  }
}
