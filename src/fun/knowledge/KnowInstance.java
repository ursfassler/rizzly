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

package fun.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;
import fun.Fun;
import fun.other.ActualTemplateArgument;

public class KnowInstance extends KnowledgeEntry {
  final private Map<Pair<Fun, List<ActualTemplateArgument>>, Fun> instances = new HashMap<Pair<Fun, List<ActualTemplateArgument>>, Fun>();

  @Override
  public void init(KnowledgeBase base) {
  }

  public Fun find(Fun fun, List<ActualTemplateArgument> param) {
    return instances.get(new Pair<Fun, List<ActualTemplateArgument>>(fun, param));
  }

  public void add(Fun fun, List<ActualTemplateArgument> param, Fun inst) {
    instances.put(new Pair<Fun, List<ActualTemplateArgument>>(fun, param), inst);
  }

  public void replace(Fun fun, List<ActualTemplateArgument> param, Fun inst) {
    instances.put(new Pair<Fun, List<ActualTemplateArgument>>(fun, param), inst);
  }
}
