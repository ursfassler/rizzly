
package parser;

/**
 *
 * @author urs
 */
public class Symbol {
  char sym;
  int line;
  int row;

  public Symbol(char sym, int line, int row) {
    this.sym = sym;
    this.line = line;
    this.row = row;
  }
}
