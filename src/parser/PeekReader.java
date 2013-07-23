package parser;

/**
 *
 * @author urs
 */
public interface PeekReader<T> {
  public T peek();
  public T next();
  public boolean hasNext();
}
