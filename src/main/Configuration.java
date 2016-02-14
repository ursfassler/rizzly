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

package main;

import ast.Designator;

public interface Configuration {

  public abstract String getRootPath();

  public abstract String getNamespace();

  public abstract Designator getRootComp();

  public abstract boolean doDebugEvent();

  public abstract boolean doLazyModelCheck();

  public abstract boolean doDocOutput();

  public abstract String getExtension();

  public abstract boolean doXml();

  public abstract FileType parseAs();

}
