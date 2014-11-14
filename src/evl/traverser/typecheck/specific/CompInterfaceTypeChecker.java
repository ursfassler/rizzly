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

package evl.traverser.typecheck.specific;

import java.util.List;

import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.Connection;
import evl.composition.Endpoint;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.function.Function;
import evl.function.InterfaceFunction;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.hfsm.ImplHfsm;
import evl.hfsm.Transition;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.traverser.ClassGetter;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.variable.Constant;

public class CompInterfaceTypeChecker extends NullTraverser<Void, Void> {

  private KnowledgeBase kb;
  private KnowType kt;

  public CompInterfaceTypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
    this.kt = kb.getEntry(KnowType.class);
  }

  public static void process(Namespace impl, KnowledgeBase kb) {
    CompInterfaceTypeChecker adder = new CompInterfaceTypeChecker(kb);
    adder.traverse(impl, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitFunctionImpl(Function obj, Void param) {
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.getChildren(), param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    // TODO can we merge with test for elementary?
    // TODO test also other stuff?
    List<Transition> transList = ClassGetter.get(Transition.class, obj);

    for (Transition tr : transList) {
      // TODO check if tr.getEventFunc() has compatible parameters
    }

    return null; // TODO check if all queries are defined
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    assert (obj.getComponent().isEmpty());
    assert (obj.getSubCallback().isEmpty());
    return null;
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    // TODO do checks over whole implementation, i.e. not splitting when functions has return value
    // TODO check for cycles
    visitList(obj.getConnection(), param);

    checkSelfIface(obj, Direction.out);
    checkSelfIface(obj, Direction.in);

    for (CompUse use : obj.getComponent()) {
      checkIface(obj, use, Direction.in);
      checkIface(obj, use, Direction.out);
    }

    return null;
  }

  private void checkSelfIface(ImplComposition obj, Direction dir) {
    for (InterfaceFunction ifaceuse : obj.getIface(dir)) {
      if (!ifaceIsConnected(ifaceuse, dir, obj.getConnection())) {
        ErrorType etype;
        if (ifaceuse instanceof FuncCtrlInDataOut) {
          etype = ErrorType.Error;
        } else {
          etype = ErrorType.Hint;
        }
        RError.err(etype, ifaceuse.getInfo(), "Interface " + ifaceuse.getName() + " not connected");
      }
    }
  }

  private Component checkIface(ImplComposition obj, CompUse use, Direction dir) {
    Component type = use.getLink();
    for (InterfaceFunction ifaceuse : type.getIface(dir)) {
      if (!ifaceIsConnected(use, ifaceuse, dir.other(), obj.getConnection())) {
        ErrorType etype;
        if (ifaceuse instanceof FuncCtrlOutDataIn) {
          etype = ErrorType.Error;
        } else {
          etype = ErrorType.Hint;
        }
        RError.err(ErrorType.Hint, ifaceuse.getInfo(), "Interface " + ifaceuse.getName() + " declared here");
        RError.err(etype, use.getInfo(), "Interface " + use.getName() + "." + ifaceuse.getName() + " not connected");
      }
    }
    return type;
  }

  private boolean ifaceIsConnected(CompUse use, InterfaceFunction ifaceuse, Direction dir, List<Connection> connection) {
    for (Connection itr : connection) {
      if (itr.getEndpoint(dir) instanceof EndpointSub) {
        EndpointSub ep = (EndpointSub) itr.getEndpoint(dir);
        if ((ep.getLink() == use) && (ep.getFunc() == ifaceuse)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean ifaceIsConnected(InterfaceFunction ifaceuse, Direction dir, List<Connection> connection) {
    for (Connection itr : connection) {
      if (itr.getEndpoint(dir) instanceof EndpointSelf) {
        Endpoint ep = itr.getEndpoint(dir);
        if (ep.getFunc() == ifaceuse) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected Void visitConnection(Connection obj, Void param) {
    Endpoint srcEp = obj.getEndpoint(Direction.in);
    Endpoint dstEp = obj.getEndpoint(Direction.out);
    Function srcType = srcEp.getFunc();
    Function dstType = dstEp.getFunc();

    Type st = kt.get(srcType);
    Type dt = kt.get(dstType);

    if (!LeftIsContainerOfRightTest.process(dt, st, kb)) {
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

  private Direction getDir(Function func) {
    if (func instanceof FuncCtrlInDataIn) {
      return Direction.in;
    } else if (func instanceof FuncCtrlInDataOut) {
      return Direction.in;
    } else if (func instanceof FuncCtrlOutDataIn) {
      return Direction.out;
    } else if (func instanceof FuncCtrlOutDataOut) {
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
