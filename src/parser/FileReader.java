/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import error.ErrorType;
import error.RError;

/**
 *
 * @author urs
 */
public class FileReader implements PeekReader<Symbol> {

  private BufferedReader stream = null;
  private int lineNr = 1;
  private int row = 1;
  private Symbol nextSym = null;

  public FileReader(String filename) {
    try {
      stream = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
      next();
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(FileReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (FileNotFoundException ex) {
      RError.err(ErrorType.Error, "Problem opening file: " + filename);
    }
  }

  private void closeFile() {
    try {
      stream.close();
      stream = null;
    } catch (IOException ex) {
      RError.err(ErrorType.Error, "Problem closing file");
      throw new RuntimeException("Problem closing file");
    }
  }

  @Override
  public Symbol peek() {
    return nextSym;
  }

  @Override
  public boolean hasNext() {
    return peek() != null;
  }

  @Override
  public Symbol next() {
    try {
      if (stream == null) {
        return null;
      }
      Symbol sym = nextSym;
      if ((sym != null) && (sym.sym == '\n')) {
        lineNr++;
        row = 1;
      } else {
        row++;
      }

      int intch = stream.read();
      if (intch == -1) {
        nextSym = null;
        closeFile();
      } else {
        nextSym = new Symbol((char) intch, lineNr, row);
      }

      return sym;
    } catch (IOException e) {
      RError.err(ErrorType.Error, "Problem reading file");
      return null;
    }
  }

  public int getLine() {
    return lineNr;
  }

  public int getRow() {
    return row;
  }
}
