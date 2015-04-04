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

package fun.toevl;

import java.util.Map;

import common.ElementInfo;

import evl.Evl;
import evl.function.Function;
import evl.function.FunctionFactory;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.function.ret.FuncReturn;
import evl.other.EvlList;
import evl.statement.Block;
import fun.Fun;
import fun.NullTraverser;
import fun.function.FuncFunction;
import fun.function.FuncHeader;
import fun.function.FuncImpl;
import fun.function.FuncProcedure;
import fun.function.FuncQuery;
import fun.function.FuncResponse;
import fun.function.FuncSignal;
import fun.function.FuncSlot;
import fun.variable.FuncVariable;

public class FunToEvlFunc extends NullTraverser<Evl, Void> {
  private Map<Fun, Evl> map;
  private FunToEvl fta;

  public FunToEvlFunc(FunToEvl fta, Map<Fun, Evl> map) {
    super();
    this.map = map;
    this.fta = fta;
  }

  @Override
  protected Evl visit(Fun obj, Void param) {
    Evl cobj = map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected Evl visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------

  public EvlList<evl.variable.FuncVariable> genpa(FuncHeader obj) {
    EvlList<evl.variable.FuncVariable> fparam = new EvlList<evl.variable.FuncVariable>();
    for (FuncVariable itr : obj.getParam()) {
      evl.variable.FuncVariable var = (evl.variable.FuncVariable) fta.traverse(itr, null);
      fparam.add(var);
    }
    return fparam;
  }

  private <T extends Function> T genfunc(FuncHeader obj, Class<T> cl) {
    FuncReturn ret = (FuncReturn) fta.traverse(obj.getRet(), null);
    T func = FunctionFactory.create(cl, obj.getInfo(), obj.getName(), genpa(obj), ret, new Block(ElementInfo.NO));
    fta.map.put(obj, func);
    if (obj instanceof FuncImpl) {
      func.body = (Block) fta.visit(((FuncImpl) obj).getBody(), null);
    }
    return func;
  }

  // ----------------------------------------------------------------------------

  @Override
  protected FuncCtrlInDataIn visitFuncSlot(FuncSlot obj, Void param) {
    return genfunc(obj, FuncCtrlInDataIn.class);
  }

  @Override
  protected FuncPrivateVoid visitFuncProcedure(FuncProcedure obj, Void param) {
    return genfunc(obj, FuncPrivateVoid.class);
  }

  @Override
  protected FuncCtrlInDataOut visitFuncResponse(FuncResponse obj, Void param) {
    return genfunc(obj, FuncCtrlInDataOut.class);
  }

  @Override
  protected FuncCtrlOutDataOut visitFuncSignal(FuncSignal obj, Void param) {
    return genfunc(obj, FuncCtrlOutDataOut.class);
  }

  @Override
  protected FuncCtrlOutDataIn visitFuncQuery(FuncQuery obj, Void param) {
    return genfunc(obj, FuncCtrlOutDataIn.class);
  }

  @Override
  protected FuncPrivateRet visitFuncFunction(FuncFunction obj, Void param) {
    return genfunc(obj, FuncPrivateRet.class);
  }

}
