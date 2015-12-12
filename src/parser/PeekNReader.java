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

import java.util.LinkedList;

public class PeekNReader<T> {
  final private PeekReader<T> scanner;
  final private LinkedList<T> tokens = new LinkedList<T>();

  public PeekNReader(PeekReader<T> scanner) {
    this.scanner = scanner;
  }

  public T next() {
    fillQueueTo(1);
    return tokens.removeFirst();
  }

  public T peek(int i) {
    fillQueueTo(i + 1);
    return tokens.get(i);
  }

  private void fillQueueTo(int n) {
    for (int i = tokens.size(); i < n; i++) {
      tokens.add(scanner.next());
    }
  }

}
