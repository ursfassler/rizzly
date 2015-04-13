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

package ast.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ast.Designator;
import ast.data.Metadata;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Direction;
import ast.data.component.composition.EndpointRaw;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.data.raw.RawComponent;
import ast.data.raw.RawComposition;
import ast.doc.compgraph.Component;
import ast.doc.compgraph.Interface;
import ast.doc.compgraph.SubComponent;
import ast.doc.compgraph.WorldComp;
import ast.knowledge.KnowPath;
import ast.knowledge.KnowledgeBase;
import ast.traverser.other.ClassGetter;
import error.ErrorType;
import error.RError;

//TODO do we need the "Self" prefix?
public class CompositionGraphMaker {
  public static final String METADATA_KEY = "geometry";

  public static WorldComp make(Designator path, String name, RawComposition impl, KnowledgeBase kb) {
    KnowPath kp = kb.getEntry(KnowPath.class);
    WorldComp comp = new WorldComp(impl.getInfo(), path, name, filterMetadata(impl.getInfo().metadata, METADATA_KEY));

    Map<CompUse, SubComponent> compmap = new HashMap<CompUse, SubComponent>();
    Map<Designator, Interface> ifacemap = new HashMap<Designator, Interface>();

    // TODO cleanup
    for (FuncResponse iface : ClassGetter.filter(FuncResponse.class, impl.getIface())) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getInput().add(niface);
    }
    for (FuncSlot iface : ClassGetter.filter(FuncSlot.class, impl.getIface())) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getInput().add(niface);
    }
    for (FuncQuery iface : ClassGetter.filter(FuncQuery.class, impl.getIface())) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getOutput().add(niface);
    }
    for (FuncSignal iface : ClassGetter.filter(FuncSignal.class, impl.getIface())) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getOutput().add(niface);
    }

    for (CompUse use : ClassGetter.filter(CompUse.class, impl.getInstantiation())) {
      ast.data.raw.RawComponent comptype = (RawComponent) ((ast.data.template.Template) ((Reference) use.compRef).link).getObject();
      Designator subpath = kp.get(comptype);
      SubComponent sub = new SubComponent(use.getInfo(), use.name, subpath, comptype.name, filterMetadata(use.getInfo().metadata, METADATA_KEY));

      // TODO cleanup
      for (FuncResponse iface : ClassGetter.filter(FuncResponse.class, comptype.getIface())) {
        Interface niface = makeIface(new Designator("Self", use.name), sub, iface, ifacemap, kb);
        sub.getInput().add(niface);
      }
      for (FuncSlot iface : ClassGetter.filter(FuncSlot.class, comptype.getIface())) {
        Interface niface = makeIface(new Designator("Self", use.name), sub, iface, ifacemap, kb);
        sub.getInput().add(niface);
      }
      for (FuncQuery iface : ClassGetter.filter(FuncQuery.class, comptype.getIface())) {
        Interface niface = makeIface(new Designator("Self", use.name), sub, iface, ifacemap, kb);
        sub.getOutput().add(niface);
      }
      for (FuncSignal iface : ClassGetter.filter(FuncSignal.class, comptype.getIface())) {
        Interface niface = makeIface(new Designator("Self", use.name), sub, iface, ifacemap, kb);
        sub.getOutput().add(niface);
      }

      compmap.put(use, sub);
      comp.getComp().add(sub);
    }

    for (Connection con : impl.getConnection()) {
      Interface src = getIface(((EndpointRaw) con.endpoint.get(Direction.in)).ref, ifacemap, kb);
      Interface dst = getIface(((EndpointRaw) con.endpoint.get(Direction.out)).ref, ifacemap, kb);
      ast.doc.compgraph.Connection ncon = new ast.doc.compgraph.Connection(src, dst, filterMetadata(con.getInfo().metadata, METADATA_KEY));
      src.getConnection().add(ncon);
      dst.getConnection().add(ncon);
      comp.getConn().add(ncon);
    }

    return comp;
  }

  private static Interface getIface(Reference ep, Map<Designator, Interface> ifacemap, ast.knowledge.KnowledgeBase kb) {
    assert (ep.offset.size() <= 1);

    Designator name = new Designator("Self", ep.link.name);
    if (!ep.offset.isEmpty()) {
      name = new Designator(name, ((ast.data.expression.reference.RefName) ep.offset.get(0)).name);
    }

    Interface iface = ifacemap.get(name);
    if (iface == null) {
      LinkedList<String> dname = new LinkedList<String>(name.toList());
      dname.pop();
      RError.err(ErrorType.Error, ep.getInfo(), "Interface not found: " + new Designator(dname));
    }
    return iface;
  }

  private static Interface makeIface(Designator name, Component sub, Function iface, Map<Designator, Interface> ifacemap, ast.knowledge.KnowledgeBase kb) {
    Interface niface = new Interface(sub, iface.name);
    name = new Designator(name, iface.name);
    assert (!ifacemap.containsKey(name));
    ifacemap.put(name, niface);
    return niface;
  }

  private static ArrayList<Metadata> filterMetadata(ArrayList<Metadata> metadata, String filterKey) {
    ArrayList<Metadata> ret = new ArrayList<Metadata>();
    for (Metadata itr : metadata) {
      if (itr.getKey().equals(filterKey)) {
        ret.add(itr);
      }
    }
    return ret;
  }

}
