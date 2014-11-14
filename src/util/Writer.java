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

package util;

public interface Writer {
  /** Write plain text **/
  public void wr(String text);

  /** Write a keyword **/
  public void kw(String name);

  /** Write a comment **/
  public void wc(String text);

  /** Write a link */
  public void wl(String text, String hint, String file, String id);

  /** Write a link */
  public void wl(String text, String hint, String file);

  /** Write an anchor (link target) */
  public void wa(String name, String id);

  public void sectionSeparator();

  public void nl();

  public void incIndent();

  public void decIndent();

}
