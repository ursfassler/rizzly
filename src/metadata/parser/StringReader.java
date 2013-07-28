package metadata.parser;

import parser.PeekReader;
import parser.Symbol;

/**
 *
 * @author urs
 */
public class StringReader implements PeekReader<Symbol> {

  private String stream;
  private int pos;
  private int lineNr = 1;
  private int row = 1;
  private Symbol nextSym = null;

  public StringReader(String stream) {
    this.stream = stream;
    pos = 0;
    next();
  }

  private void closeFile() {
    stream = null;
  }

  public Symbol peek() {
    return nextSym;
  }

  public boolean hasNext() {
    return peek() != null;
  }

  public Symbol next() {
    if (stream == null) {
      return null;
    }
    Symbol sym = nextSym;
    row++;

    if (pos >= stream.length()) {
      nextSym = null;
      closeFile();
    } else {
      nextSym = new Symbol(stream.charAt(pos), lineNr, row);
    }
    pos++;

    return sym;
  }

  public int getLine() {
    return lineNr;
  }

  public int getRow() {
    return row;
  }
}
