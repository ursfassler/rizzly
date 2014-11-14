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

package fun.other;

import common.ElementInfo;

import fun.Fun;
import fun.FunBase;
import fun.content.CompIfaceContent;
import fun.content.FileContent;

abstract public class CompImpl extends FunBase implements FileContent, Named {
  private String name;
  private final FunList<Fun> objects = new FunList<Fun>();

  public CompImpl(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public FunList<Fun> getObjects() {
    return objects;
  }

  public abstract FunList<CompIfaceContent> getInterface();

  static protected FunList<CompIfaceContent> findInterface(FunList<Fun> instantiation) {
    return instantiation.getItems(CompIfaceContent.class);
  }
}
