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

package fun.doc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.Designator;
import common.Direction;

import error.ErrorType;
import error.RError;
import fun.composition.ImplComposition;
import fun.doc.compgraph.Component;
import fun.doc.compgraph.Interface;
import fun.doc.compgraph.SubComponent;
import fun.doc.compgraph.WorldComp;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.function.FuncHeader;
import fun.function.FuncQuery;
import fun.function.FuncResponse;
import fun.function.FuncSignal;
import fun.function.FuncSlot;
import fun.knowledge.KnowFunPath;
import fun.knowledge.KnowledgeBase;
import fun.other.CompImpl;
import fun.variable.CompUse;

//TODO do we need the "Self" prefix?
public class CompositionGraphMaker {
  public static final String METADATA_KEY = "geometry";

  public static WorldComp make(Designator path, String name, ImplComposition impl, KnowledgeBase kb) {
    KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    WorldComp comp = new WorldComp(impl.getInfo(), path, name, impl.getInfo().getMetadata(METADATA_KEY));

    Map<CompUse, SubComponent> compmap = new HashMap<CompUse, SubComponent>();
    Map<Designator, Interface> ifacemap = new HashMap<Designator, Interface>();

    // TODO cleanup
    for (FuncResponse iface : impl.getIface().getItems(FuncResponse.class)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getInput().add(niface);
    }
    for (FuncSlot iface : impl.getIface().getItems(FuncSlot.class)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getInput().add(niface);
    }
    for (FuncQuery iface : impl.getIface().getItems(FuncQuery.class)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getOutput().add(niface);
    }
    for (FuncSignal iface : impl.getIface().getItems(FuncSignal.class)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getOutput().add(niface);
    }

    for (CompUse use : impl.getInstantiation().getItems(CompUse.class)) {
      fun.other.CompImpl comptype = (CompImpl) ((fun.other.Template) use.getType().getLink()).getObject();
      Designator subpath = kp.get(comptype);
      SubComponent sub = new SubComponent(use.getInfo(), use.getName(), subpath, comptype.getName(), use.getInfo().getMetadata(METADATA_KEY));

      // TODO cleanup
      for (FuncResponse iface : comptype.getIface().getItems(FuncResponse.class)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
        sub.getInput().add(niface);
      }
      for (FuncSlot iface : comptype.getIface().getItems(FuncSlot.class)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
        sub.getInput().add(niface);
      }
      for (FuncQuery iface : comptype.getIface().getItems(FuncQuery.class)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
        sub.getOutput().add(niface);
      }
      for (FuncSignal iface : comptype.getIface().getItems(FuncSignal.class)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
        sub.getOutput().add(niface);
      }

      compmap.put(use, sub);
      comp.getComp().add(sub);
    }

    for (fun.composition.Connection con : impl.getConnection()) {
      Interface src = getIface(con.getEndpoint(Direction.in), ifacemap, kb);
      Interface dst = getIface(con.getEndpoint(Direction.out), ifacemap, kb);
      fun.doc.compgraph.Connection ncon = new fun.doc.compgraph.Connection(src, dst, con.getInfo().getMetadata(METADATA_KEY));
      src.getConnection().add(ncon);
      dst.getConnection().add(ncon);
      comp.getConn().add(ncon);
    }

    return comp;
  }

  private static Interface getIface(Reference ep, Map<Designator, Interface> ifacemap, KnowledgeBase kb) {
    assert (ep.getOffset().size() <= 1);

    Designator name = new Designator("Self", ep.getLink().getName());
    if (!ep.getOffset().isEmpty()) {
      name = new Designator(name, ((RefName) ep.getOffset().get(0)).getName());
    }

    Interface iface = ifacemap.get(name);
    if (iface == null) {
      LinkedList<String> dname = new LinkedList<String>(name.toList());
      dname.pop();
      RError.err(ErrorType.Error, ep.getInfo(), "Interface not found: " + new Designator(dname));
    }
    return iface;
  }

  private static Interface makeIface(Designator name, Component sub, FuncHeader iface, Map<Designator, Interface> ifacemap, KnowledgeBase kb) {
    Interface niface = new Interface(sub, iface.getName());
    name = new Designator(name, iface.getName());
    assert (!ifacemap.containsKey(name));
    ifacemap.put(name, niface);
    return niface;
  }

}
