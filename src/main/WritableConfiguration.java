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

public class WritableConfiguration implements Configuration {
  private String extension;
  private String rootPath;
  private String namespace;
  private Designator rootComp;
  private boolean debugEvent;
  private boolean docOutput;
  private boolean lazyModelCheck;
  private boolean xml;
  private FileType fileType;

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

  @Override
  public String getExtension() {
    return extension;
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

  public void setExtension(String extension) {
    this.extension = extension;
  }

  @Override
  public boolean doXml() {
    return xml;
  }

  public void setXml(boolean xml) {
    this.xml = xml;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String value) {
    namespace = value;
  }

  @Override
  public FileType parseAs() {
    return fileType;
  }

  public void setFileType(FileType value) {
    fileType = value;
  }

}
