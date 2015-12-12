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

package parser.hfsm;

import java.util.LinkedList;

import parser.PeekReader;

public class Scanner_Dummy<T> implements PeekReader<T> {

  final private LinkedList<T> queue = new LinkedList<T>();
  final private T endElement;

  public Scanner_Dummy(T endElement) {
    super();
    this.endElement = endElement;
  }

  public void add(T token) {
    queue.add(token);
  }

  @Override
  public T peek() {
    if (queue.isEmpty()) {
      return endElement;
    }

    return queue.peek();
  }

  @Override
  public T next() {
    if (queue.isEmpty()) {
      return endElement;
    }

    return queue.removeFirst();
  }

  @Override
  public boolean hasNext() {
    return queue.isEmpty();
  }

}
