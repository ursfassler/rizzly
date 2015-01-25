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

package evl.traverser;

import java.util.HashMap;
import java.util.Map;

import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.function.Function;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.Namespace;
import evl.other.SubCallbacks;

public class CompInstantiator extends NullTraverser<ImplElementary, Namespace> {
  final private Map<Named, Named> linkmap = new HashMap<Named, Named>();

  public static void process(ImplElementary root, Namespace container) {
    CompInstantiator instantiator = new CompInstantiator();
    instantiator.traverse(root, container);
    Relinker.relink(container, instantiator.linkmap);
  }

  @Override
  protected ImplElementary visitDefault(Evl obj, Namespace param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ImplElementary visitImplElementary(ImplElementary obj, Namespace ns) {
    ImplElementary inst = Copy.copy(obj);

    ns.addAll(inst.getIface());
    ns.addAll(inst.getType());
    ns.addAll(inst.getConstant());
    ns.addAll(inst.getVariable());
    ns.addAll(inst.getFunction());
    ns.add(inst.getQueue());
    ns.add(inst.getEntryFunc());
    ns.add(inst.getExitFunc());

    // ns.getChildren().removeAll(ns.getChildren().getItems(FuncCtrlOutDataIn.class));
    // ns.getChildren().removeAll(ns.getChildren().getItems(FuncCtrlOutDataOut.class));

    for (CompUse compUse : inst.getComponent()) {
      Component comp = compUse.getLink();

      // copy / instantiate used component
      Namespace usens = new Namespace(compUse.getInfo(), compUse.getName());
      ImplElementary cpy = visit(comp, usens);
      ns.add(usens);
      linkmap.put(compUse, usens);

      // route output interface to sub-callback implementation
      for (Function impl : getSubCallback(inst.getSubCallback(), compUse).getFunc()) {
        // get output declaration of instantiated sub-component
        Function outdecl = (Function) cpy.getIface().find(impl.getName());

        assert (outdecl != null);
        assert (usens.getChildren().contains(outdecl));
        assert (!linkmap.containsKey(outdecl));

        // change links to output declaration to the sub-callback of this component
        linkmap.put(outdecl, impl);
        usens.getChildren().remove(outdecl);
        usens.add(impl);
      }
    }

    return inst;
  }

  private SubCallbacks getSubCallback(EvlList<SubCallbacks> subCallback, CompUse compUse) {
    for (SubCallbacks suc : subCallback) {
      if (suc.getCompUse().getLink() == compUse) {
        return suc;
      }
    }
    assert (false);
    return null;
  }

}
