
package parser;

/**
 *
 * @author urs
 */
public class Symbol {
  public final char sym;
  public final int line;
  public final int row;

  public Symbol(char sym, int line, int row) {
    this.sym = sym;
    this.line = line;
    this.row = row;
  }
}
