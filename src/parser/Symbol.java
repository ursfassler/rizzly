
package parser;

/**
 *
 * @author urs
 */
public class Symbol {
  final char sym;
  final int line;
  final int row;

  public Symbol(char sym, int line, int row) {
    this.sym = sym;
    this.line = line;
    this.row = row;
  }
}
