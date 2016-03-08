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

package ast.pass.input.xml.linker;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ast.data.Named;
import error.ErrorType;
import error.RizzlyError;

public class XmlIdMatcher implements ObjectRegistrar, LinkDummyRecorder {
  final private Map<String, Named> idToObject = new HashMap<>();
  final private Set<LinkDummy> targets = new HashSet<>();
  final private RizzlyError error;

  public XmlIdMatcher(RizzlyError error) {
    this.error = error;
  }

  @Override
  public void register(String id, Named object) {
    if ("".equals(id)) {
      return;
    }

    if (idToObject.containsKey(id)) {
      Named first = idToObject.get(id);
      error.err(ErrorType.Hint, "First defined here", first.metadata());
      error.err(ErrorType.Error, "Object with the id \"" + id + "\" already registered", object.metadata());
    } else {
      idToObject.put(id, object);
    }
  }

  @Override
  public void add(LinkDummy link) {
    targets.add(link);
  }

  public Map<String, Named> getIdToObject() {
    return idToObject;
  }

  public Collection<LinkDummy> getTargets() {
    return targets;
  }

  public Map<LinkDummy, Named> getMapping() {
    Map<LinkDummy, Named> map = new HashMap<>();
    for (LinkDummy dummy : targets) {
      String id = dummy.getName();
      if (idToObject.containsKey(id)) {
        map.put(dummy, idToObject.get(id));
      } else {
        error.err(ErrorType.Error, "no object with id \"" + id + "\" defined", dummy.metadata());
      }
    }
    return map;
  }

}
