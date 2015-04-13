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

package evl.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;
import evl.data.Evl;
import fun.other.ActualTemplateArgument;

public class KnowInstance extends KnowledgeEntry {
  final private Map<Pair<Evl, List<ActualTemplateArgument>>, Evl> instances = new HashMap<Pair<Evl, List<ActualTemplateArgument>>, Evl>();

  @Override
  public void init(evl.knowledge.KnowledgeBase base) {
  }

  public Evl find(Evl fun, List<ActualTemplateArgument> param) {
    return instances.get(new Pair<Evl, List<ActualTemplateArgument>>(fun, param));
  }

  public void add(Evl fun, List<ActualTemplateArgument> param, Evl inst) {
    instances.put(new Pair<Evl, List<ActualTemplateArgument>>(fun, param), inst);
  }

  public void replace(Evl fun, List<ActualTemplateArgument> param, Evl inst) {
    instances.put(new Pair<Evl, List<ActualTemplateArgument>>(fun, param), inst);
  }
}
