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

import java.util.List;

import ast.Designator;

public class WritableConfiguration implements Configuration {
  private String rootPath;
  private String namespace;
  private Designator rootComp;
  private boolean debugEvent;
  private boolean docOutput;
  private boolean lazyModelCheck;
  private PassBuilding fileType;
  private List<String> passes;

  @Override
  public String getRootPath() {
    return rootPath;
  }

  @Override
  public Designator getRootComp() {
    return rootComp;
  }

  @Override
  public boolean doDebugEvent() {
    return isDebugEvent();
  }

  @Override
  public boolean doLazyModelCheck() {
    return isLazyModelCheck();
  }

  @Override
  public boolean doDocOutput() {
    return isDocOutput();
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public void setRootComp(Designator rootComp) {
    this.rootComp = rootComp;
  }

  public boolean isDebugEvent() {
    return debugEvent;
  }

  public void setDebugEvent(boolean debugEvent) {
    this.debugEvent = debugEvent;
  }

  public boolean isDocOutput() {
    return docOutput;
  }

  public void setDocOutput(boolean docOutput) {
    this.docOutput = docOutput;
  }

  public boolean isLazyModelCheck() {
    return lazyModelCheck;
  }

  public void setLazyModelCheck(boolean lazyModelCheck) {
    this.lazyModelCheck = lazyModelCheck;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String value) {
    namespace = value;
  }

  @Override
  public PassBuilding passBuilding() {
    return fileType;
  }

  public void setPassBuilding(PassBuilding value) {
    fileType = value;
  }

  @Override
  public List<String> passes() {
    return passes;
  }

  public void setPasses(List<String> value) {
    passes = value;
  }
}
