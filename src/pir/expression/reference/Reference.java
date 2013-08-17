package pir.expression.reference;


public interface Reference<T extends Referencable> {
  public T getRef();

  public void setRef(T ref);

}
