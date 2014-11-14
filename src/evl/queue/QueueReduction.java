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

package evl.queue;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Designator;
import common.ElementInfo;
import common.Property;

import evl.DefTraverser;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.expression.ArrayValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.binop.Greater;
import evl.expression.binop.Less;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Plus;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncPrivateVoid;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.other.Queue;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptEntry;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.intern.MsgPush;
import evl.traverser.ClassGetter;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

public class QueueReduction {

  public static void process(Namespace classes, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    { // TODO keep queues of active components
      Queue queue = new Queue();
      Map<Queue, Queue> map = new HashMap<Queue, Queue>();
      for (Queue old : ClassGetter.get(Queue.class, classes)) {
        map.put(old, queue);
      }
      Relinker.relink(classes, map);
      classes.add(queue);
    }

    List<MsgPush> pushes = ClassGetter.get(MsgPush.class, classes);

    // Find destination functions
    Map<Queue, Set<Function>> queues = new HashMap<Queue, Set<Function>>();
    Set<Function> funcs = new HashSet<Function>();
    for (MsgPush push : pushes) {
      Queue queue = (Queue) push.getQueue().getLink();
      Set<Function> set = queues.get(queue);
      if (set == null) {
        set = new HashSet<Function>();
        queues.put(queue, set);
      }
      Function func = (Function) push.getFunc().getLink();
      set.add(func);
      funcs.add(func);
    }

    // create msg content types
    Map<Function, RecordType> funrec = new HashMap<Function, RecordType>();
    for (Function func : funcs) {
      // Designator path = kp.get(func);
      // assert (path.size() > 0);
      // String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);
      String name = Integer.toString(func.hashCode());

      EvlList<NamedElement> elements = new EvlList<NamedElement>();
      for (FuncVariable arg : func.getParam()) {
        NamedElement elem = new NamedElement(arg.getInfo(), arg.getName(), new SimpleRef<Type>(ElementInfo.NO, arg.getType().getLink()));
        elements.add(elem);
      }
      RecordType rec = new RecordType(func.getInfo(), Designator.NAME_SEP + "msg" + Designator.NAME_SEP + name, elements);

      funrec.put(func, rec);

      classes.add(rec);
    }

    Map<Function, Function> pushfunc = new HashMap<Function, Function>();

    for (Queue queue : queues.keySet()) {
      // create queue type (union of records)
      String prefix = queue.getName() + Designator.NAME_SEP;

      ElementInfo info = queue.getInfo();
      EnumType msgType = new EnumType(info, prefix + "msgid");
      Map<Function, NamedElement> elements = new HashMap<Function, NamedElement>();

      Map<Function, EnumElement> funem = new HashMap<Function, EnumElement>();

      NamedElement tag = new NamedElement(info, "_tag", new SimpleRef<Type>(info, msgType));

      EvlList<NamedElement> unielem = new EvlList<NamedElement>();
      for (Function func : queues.get(queue)) {
        // TODO add name
        // Designator path = kp.get(func);
        // assert (path.size() > 0);
        // String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);
        // String name = new Designator(path, Integer.toString(func.hashCode())).toString(Designator.NAME_SEP);

        NamedElement elem = new NamedElement(info, func.getName(), new SimpleRef<Type>(info, funrec.get(func)));
        elements.put(func, elem);
        unielem.add(elem);

        EnumElement enumElem = new EnumElement(info, prefix + func.getName());
        msgType.getElement().add(enumElem);

        funem.put(func, enumElem);
      }
      UnionType uni = new UnionType(info, prefix + "queue", unielem, tag);

      classes.add(msgType);
      classes.add(uni);

      // create queue

      int queueSize = 20;  // FIXME remove magic
      // create queue variable
      ArrayType qat = new ArrayType(BigInteger.valueOf(queueSize), new SimpleRef<Type>(info, uni));
      classes.add(qat);
      StateVariable qv = new StateVariable(info, prefix + "vdata", new SimpleRef<Type>(info, qat), new ArrayValue(info, new EvlList<Expression>()));
      classes.add(qv);

      StateVariable qh = new StateVariable(info, prefix + "vhead", new SimpleRef<Type>(info, kbi.getRangeType(queueSize)), new Number(info, BigInteger.ZERO));
      classes.add(qh);

      StateVariable qc = new StateVariable(info, prefix + "vcount", new SimpleRef<Type>(info, kbi.getRangeType(queueSize + 1)), new Number(info, BigInteger.ZERO));
      classes.add(qc);

      // function to return number of elements in queue
      Block sfb = new Block(info);
      sfb.getStatements().add(new ReturnExpr(info, new Reference(info, qc)));
      Function sizefunc = new FuncCtrlInDataOut(info, prefix + "count", new EvlList<FuncVariable>(), new SimpleRef<Type>(ElementInfo.NO, qc.getType().getLink()), sfb);
      sizefunc.properties().put(Property.Public, true);
      classes.add(sizefunc);

      // create dispatch function

      Block body = createDispatchBody(funem, funrec, elements, qv, qh, qc);
      Function dispatcher = new FuncCtrlInDataIn(info, prefix + "dispatch", new EvlList<FuncVariable>(), new SimpleRef<Type>(info, kbi.getVoidType()), body);
      dispatcher.properties().put(Property.Public, true);
      classes.add(dispatcher);

      // create push functions

      for (Function func : queues.get(queue)) {
        // Designator path = kp.get(func);
        // assert (path.size() > 0);
        // String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);
        String name = func.getName();

        EvlList<FuncVariable> param = Copy.copy(func.getParam());
        Function impl = new FuncPrivateVoid(info, Designator.NAME_SEP + "push" + Designator.NAME_SEP + name, param, new SimpleRef<Type>(info, kbi.getVoidType()), createPushBody(param, qv, qh, qc, queueSize, uni, msgType, funem.get(func), elements.get(func)));
        // impl.properties().put(Property.NAME, Designator.NAME_SEP + "push" + Designator.NAME_SEP + name);

        classes.add(impl);
        pushfunc.put(func, impl);
      }

    }

    PushReplacer pr = new PushReplacer();
    pr.traverse(classes, pushfunc);
  }

  private static Block createPushBody(EvlList<FuncVariable> param, StateVariable qv, StateVariable qh, StateVariable qc, int queueSize, UnionType uni, EnumType msgType, EnumElement enumElement, NamedElement namedElement) {
    ElementInfo info = ElementInfo.NO;

    EvlList<IfOption> option = new EvlList<IfOption>();

    Block pushbody = new Block(info);

    FuncVariable idx = new FuncVariable(info, "wridx", new SimpleRef<Type>(info, qh.getType().getLink()));
    pushbody.getStatements().add(new VarDefStmt(info, idx));
    pushbody.getStatements().add(new Assignment(info, new Reference(info, idx), new Mod(info, new Plus(info, new Reference(info, qh), new Reference(info, qc)), new Number(info, BigInteger.valueOf(queueSize)))));

    Reference qir = new Reference(info, qv);
    qir.getOffset().add(new RefIndex(info, new Reference(info, idx)));
    qir.getOffset().add(new RefName(info, uni.getTag().getName()));
    pushbody.getStatements().add(new Assignment(info, qir, new Reference(info, enumElement)));

    for (FuncVariable arg : param) {
      Reference elem = new Reference(info, qv);
      elem.getOffset().add(new RefIndex(info, new Reference(info, idx)));
      elem.getOffset().add(new RefName(info, namedElement.getName()));
      elem.getOffset().add(new RefName(info, arg.getName()));

      pushbody.getStatements().add(new Assignment(info, elem, new Reference(info, arg)));
    }

    pushbody.getStatements().add(new Assignment(info, new Reference(info, qc), new Plus(info, new Reference(info, qc), new Number(info, BigInteger.ONE))));

    IfOption ifok = new IfOption(info, new Less(info, new Reference(info, qc), new Number(info, BigInteger.valueOf(queueSize))), pushbody);
    option.add(ifok);

    Block body = new Block(info);
    body.getStatements().add(new IfStmt(info, option, new Block(info))); // TODO add error code
    return body;
  }

  private static Block createDispatchBody(Map<Function, EnumElement> funem, Map<Function, RecordType> funrec, Map<Function, NamedElement> elements, StateVariable qdata, StateVariable qhead, StateVariable qsize) {
    ElementInfo info = ElementInfo.NO;

    Block body = new Block(info);

    ArrayType dt = (ArrayType) qdata.getType().getLink();
    UnionType ut = (UnionType) dt.getType().getLink();

    EvlList<CaseOpt> opt = new EvlList<CaseOpt>();
    Reference ref = new Reference(info, qdata);
    ref.getOffset().add(new RefIndex(info, new Reference(info, qhead)));
    ref.getOffset().add(new RefName(info, ut.getTag().getName()));
    CaseStmt caseStmt = new CaseStmt(info, ref, opt, new Block(info));

    for (Function func : funem.keySet()) {
      EvlList<CaseOptEntry> value = new EvlList<CaseOptEntry>();
      value.add(new CaseOptValue(info, new Reference(info, funem.get(func))));
      CaseOpt copt = new CaseOpt(info, value, new Block(info));

      NamedElement un = elements.get(func);
      RecordType rec = funrec.get(func);
      EvlList<Expression> acarg = new EvlList<Expression>();
      for (NamedElement elem : rec.getElement()) {
        Reference vref = new Reference(info, qdata);
        vref.getOffset().add(new RefIndex(info, new Reference(info, qhead)));
        vref.getOffset().add(new RefName(info, un.getName()));
        vref.getOffset().add(new RefName(info, elem.getName()));
        acarg.add(vref);
      }

      Reference call = new Reference(info, func);
      call.getOffset().add(new RefCall(info, acarg));
      copt.getCode().getStatements().add(new CallStmt(info, call));

      caseStmt.getOption().add(copt);
    }

    Assignment add = new Assignment(info, new Reference(info, qhead), new Plus(info, new Reference(info, qhead), new Number(info, BigInteger.ONE)));
    Assignment sub = new Assignment(info, new Reference(info, qsize), new Minus(info, new Reference(info, qsize), new Number(info, BigInteger.ONE)));

    EvlList<IfOption> option = new EvlList<IfOption>();
    IfOption ifOption = new IfOption(info, new Greater(info, new Reference(info, qsize), new Number(info, BigInteger.ZERO)), new Block(info));
    ifOption.getCode().getStatements().add(caseStmt);
    ifOption.getCode().getStatements().add(sub);
    ifOption.getCode().getStatements().add(add);

    option.add(ifOption);
    IfStmt ifc = new IfStmt(info, option, new Block(info));

    body.getStatements().add(ifc);
    return body;
  }
}

class PushReplacer extends DefTraverser<Statement, Map<Function, Function>> {

  @Override
  protected Statement visitBlock(Block obj, Map<Function, Function> param) {
    for (int i = 0; i < obj.getStatements().size(); i++) {
      Statement stmt = visit(obj.getStatements().get(i), param);
      if (stmt != null) {
        obj.getStatements().set(i, stmt);
      }
    }
    return null;
  }

  @Override
  protected Statement visitMsgPush(MsgPush obj, Map<Function, Function> param) {
    assert (param.containsKey(obj.getFunc().getLink()));

    Reference call = new Reference(obj.getInfo(), param.get(obj.getFunc().getLink()));
    call.getOffset().add(new RefCall(obj.getInfo(), obj.getData()));

    return new CallStmt(obj.getInfo(), call);
  }

}
