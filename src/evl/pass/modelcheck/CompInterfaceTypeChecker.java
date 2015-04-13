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

package evl.pass.modelcheck;

import java.util.List;

import pass.EvlPass;

import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.Namespace;
import evl.data.component.Component;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.Connection;
import evl.data.component.composition.Endpoint;
import evl.data.component.composition.EndpointSelf;
import evl.data.component.composition.EndpointSub;
import evl.data.component.composition.ImplComposition;
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.Transition;
import evl.data.function.Function;
import evl.data.function.InterfaceFunction;
import evl.data.function.header.FuncQuery;
import evl.data.function.header.FuncResponse;
import evl.data.function.header.FuncSignal;
import evl.data.function.header.FuncSlot;
import evl.data.type.Type;
import evl.data.variable.Constant;
import evl.knowledge.KnowLeftIsContainerOfRight;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import evl.traverser.other.ClassGetter;

public class CompInterfaceTypeChecker extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    CompInterfaceTypeCheckerWorker adder = new CompInterfaceTypeCheckerWorker(kb);
    adder.traverse(evl, null);
  }

}

class CompInterfaceTypeCheckerWorker extends NullTraverser<Void, Void> {

  final private KnowType kt;
  final private KnowLeftIsContainerOfRight kc;

  public CompInterfaceTypeCheckerWorker(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
    this.kc = kb.getEntry(KnowLeftIsContainerOfRight.class);
  }

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitCompUse(CompUse obj, Void param) {
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Void param) {
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.children, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    // TODO can we merge with test for elementary?
    // TODO test also other stuff?
    List<Transition> transList = ClassGetter.getRecursive(Transition.class, obj);

    for (Transition tr : transList) {
      // TODO check if tr.getEventFunc() has compatible parameters
    }

    return null; // TODO check if all queries are defined
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    assert (obj.component.isEmpty());
    assert (obj.subCallback.isEmpty());
    return null;
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    // TODO do checks over whole implementation, i.e. not splitting when
    // functions has return value
    // TODO check for cycles
    visitList(obj.connection, param);

    checkSelfIface(obj, Direction.out);
    checkSelfIface(obj, Direction.in);

    for (CompUse use : obj.component) {
      checkIface(obj, use, Direction.in);
      checkIface(obj, use, Direction.out);
    }

    return null;
  }

  private void checkSelfIface(ImplComposition obj, Direction dir) {
    for (InterfaceFunction ifaceuse : obj.getIface(dir)) {
      if (!ifaceIsConnected(ifaceuse, dir, obj.connection)) {
        ErrorType etype;
        if (ifaceuse instanceof FuncResponse) {
          etype = ErrorType.Error;
        } else {
          etype = ErrorType.Hint;
        }
        RError.err(etype, ifaceuse.getInfo(), "Interface " + ifaceuse.name + " not connected");
      }
    }
  }

  private Component checkIface(ImplComposition obj, CompUse use, Direction dir) {
    Component type = (Component) use.compRef.getTarget();
    for (InterfaceFunction ifaceuse : type.getIface(dir)) {
      if (!ifaceIsConnected(use, ifaceuse, dir.other(), obj.connection)) {
        ErrorType etype;
        if (ifaceuse instanceof FuncQuery) {
          etype = ErrorType.Error;
        } else {
          etype = ErrorType.Hint;
        }
        RError.err(ErrorType.Hint, ifaceuse.getInfo(), "Interface " + ifaceuse.name + " declared here");
        RError.err(etype, use.getInfo(), "Interface " + use.name + "." + ifaceuse.name + " not connected");
      }
    }
    return type;
  }

  private boolean ifaceIsConnected(CompUse use, InterfaceFunction ifaceuse, Direction dir, List<Connection> connection) {
    for (Connection itr : connection) {
      if (itr.endpoint.get(dir) instanceof EndpointSub) {
        EndpointSub ep = (EndpointSub) itr.endpoint.get(dir);
        if ((ep.component.link == use) && (ep.getFunc() == ifaceuse)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean ifaceIsConnected(InterfaceFunction ifaceuse, Direction dir, List<Connection> connection) {
    for (Connection itr : connection) {
      if (itr.endpoint.get(dir) instanceof EndpointSelf) {
        Endpoint ep = itr.endpoint.get(dir);
        if (ep.getFunc() == ifaceuse) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected Void visitConnection(Connection obj, Void param) {
    Endpoint srcEp = obj.endpoint.get(Direction.in);
    Endpoint dstEp = obj.endpoint.get(Direction.out);
    Function srcType = getIfaceFunc(srcEp);
    Function dstType = getIfaceFunc(dstEp);

    Type st = kt.get(srcType);
    Type dt = kt.get(dstType);

    if (!kc.get(dt, st)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Invalid connection: " + st + " -> " + dt);
    }

    boolean srcSelf = srcEp instanceof EndpointSelf;
    boolean dstSelf = dstEp instanceof EndpointSelf;
    Direction srcDir = getDir(srcType);
    Direction dstDir = getDir(dstType);

    if (srcSelf && dstSelf) {
      checkDir(srcDir, Direction.in, Direction.in, srcEp.getInfo());
      checkDir(dstDir, Direction.out, Direction.out, dstEp.getInfo());
    } else if (!srcSelf && dstSelf) {
      checkDir(srcDir, Direction.out, Direction.in, srcEp.getInfo());
      checkDir(dstDir, Direction.out, Direction.out, dstEp.getInfo());
    } else if (srcSelf && !dstSelf) {
      checkDir(srcDir, Direction.in, Direction.in, srcEp.getInfo());
      checkDir(dstDir, Direction.in, Direction.out, dstEp.getInfo());
    } else {
      assert (!srcSelf && !dstSelf);
      checkDir(srcDir, Direction.out, Direction.in, srcEp.getInfo());
      checkDir(dstDir, Direction.in, Direction.out, dstEp.getInfo());
    }

    return null;
  }

  private Function getIfaceFunc(Endpoint ep) {
    Function func = ep.getFunc();
    if (func == null) {
      RError.err(ErrorType.Error, ep.getInfo(), "Interface not found: " + ep.toString());
    }
    return func;
  }

  private Direction getDir(Function func) {
    if (func instanceof FuncSlot) {
      return Direction.in;
    } else if (func instanceof FuncResponse) {
      return Direction.in;
    } else if (func instanceof FuncQuery) {
      return Direction.out;
    } else if (func instanceof FuncSignal) {
      return Direction.out;
    } else {
      RError.err(ErrorType.Fatal, func.getInfo(), "Unexpected function type: " + func.getClass().getCanonicalName());
      return null;
    }
  }

  private void checkDir(Direction is, Direction should, Direction ep, ElementInfo info) {
    if (is != should) {
      String eps = ep == Direction.in ? "from" : "to";
      String iss = is == Direction.in ? "input" : "output";
      RError.err(ErrorType.Error, info, "can not connect " + eps + " " + iss);
    }
  }

}
