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
import ast.data.AstList;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.EndpointRaw;
import ast.data.function.Function;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.Response;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.raw.RawComponent;
import ast.data.raw.RawComposition;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.RefItem;
import ast.data.reference.Reference;
import ast.data.reference.ReferenceOffset;
import ast.doc.compgraph.Component;
import ast.doc.compgraph.Interface;
import ast.doc.compgraph.SubComponent;
import ast.doc.compgraph.WorldComp;
import ast.knowledge.KnowPath;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
import ast.meta.Metadata;
import ast.repository.query.TypeFilter;
import error.ErrorType;
import error.RError;

//TODO do we need the "Self" prefix?
public class CompositionGraphMaker {
  public static final String METADATA_KEY = "geometry";

  public static WorldComp make(Designator path, String name, RawComposition impl, KnowledgeBase kb) {
    KnowPath kp = kb.getEntry(KnowPath.class);
    WorldComp comp = new WorldComp(impl.metadata(), path, name, filterMetadata(impl.metadata(), METADATA_KEY));

    Map<ComponentUse, SubComponent> compmap = new HashMap<ComponentUse, SubComponent>();
    Map<Designator, Interface> ifacemap = new HashMap<Designator, Interface>();

    // TODO cleanup
    for (Response iface : TypeFilter.select(impl.getIface(), Response.class)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getInput().add(niface);
    }
    for (Slot iface : TypeFilter.select(impl.getIface(), Slot.class)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getInput().add(niface);
    }
    for (FuncQuery iface : TypeFilter.select(impl.getIface(), FuncQuery.class)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getOutput().add(niface);
    }
    for (Signal iface : TypeFilter.select(impl.getIface(), Signal.class)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
      comp.getOutput().add(niface);
    }

    for (ComponentUse use : TypeFilter.select(impl.getInstantiation(), ComponentUse.class)) {
      ast.data.raw.RawComponent comptype = (RawComponent) ((LinkedAnchor) use.compRef.getAnchor()).getLink();
      Designator subpath = kp.get(comptype);
      SubComponent sub = new SubComponent(use.metadata(), use.getName(), subpath, comptype.getName(), filterMetadata(use.metadata(), METADATA_KEY));

      // TODO cleanup
      for (Response iface : TypeFilter.select(comptype.getIface(), Response.class)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
        sub.getInput().add(niface);
      }
      for (Slot iface : TypeFilter.select(comptype.getIface(), Slot.class)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
        sub.getInput().add(niface);
      }
      for (FuncQuery iface : TypeFilter.select(comptype.getIface(), FuncQuery.class)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
        sub.getOutput().add(niface);
      }
      for (Signal iface : TypeFilter.select(comptype.getIface(), Signal.class)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
        sub.getOutput().add(niface);
      }

      compmap.put(use, sub);
      comp.getComp().add(sub);
    }

    for (Connection con : impl.getConnection()) {
      Interface src = getIface(((EndpointRaw) (con.getSrc())).ref, ifacemap, kb);
      Interface dst = getIface(((EndpointRaw) (con.getDst())).ref, ifacemap, kb);
      ast.doc.compgraph.Connection ncon = new ast.doc.compgraph.Connection(src, dst, filterMetadata(con.metadata(), METADATA_KEY));
      src.getConnection().add(ncon);
      dst.getConnection().add(ncon);
      comp.getConn().add(ncon);
    }

    return comp;
  }

  private static Interface getIface(Reference ep, Map<Designator, Interface> ifacemap, ast.knowledge.KnowledgeBase kb) {
    AstList<RefItem> offset = ((ReferenceOffset) ep).getOffset();

    assert (offset.size() <= 1);

    Designator name = new Designator("Self", ((LinkedAnchor) ep.getAnchor()).getLink().getName());
    if (!offset.isEmpty()) {
      name = new Designator(name, ((ast.data.reference.RefName) offset.get(0)).name);
    }

    Interface iface = ifacemap.get(name);
    if (iface == null) {
      LinkedList<String> dname = new LinkedList<String>(name.toList());
      dname.pop();
      RError.err(ErrorType.Error, "Interface not found: " + new Designator(dname), ep.metadata());
    }
    return iface;
  }

  private static Interface makeIface(Designator name, Component sub, Function iface, Map<Designator, Interface> ifacemap, ast.knowledge.KnowledgeBase kb) {
    Interface niface = new Interface(sub, iface.getName());
    name = new Designator(name, iface.getName());
    assert (!ifacemap.containsKey(name));
    ifacemap.put(name, niface);
    return niface;
  }

  private static ArrayList<Metadata> filterMetadata(MetaList metadata, String filterKey) {
    // ArrayList<Metadata> ret = new ArrayList<Metadata>();
    // for (Metadata itr : metadata) {
    // if (itr.getKey().equals(filterKey)) {
    // ret.add(itr);
    // }
    // }
    // return ret;
    RError.err(ErrorType.Fatal, "fixme");
    throw new RuntimeException();
  }

}
