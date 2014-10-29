package error;

/**
 * 
 * @author urs
 */
public enum ErrorType {
  Hint, // Information
  Warning, // error, but can continue
  Error, // Can't continue
  Fatal, // Internal error
  Assertion;
}
