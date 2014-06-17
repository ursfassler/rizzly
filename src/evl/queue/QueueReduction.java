package evl.queue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Designator;
import common.ElementInfo;
import common.FuncAttr;

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
import evl.function.FunctionHeader;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateVoid;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowPath;
import evl.knowledge.KnowledgeBase;
import evl.other.ListOfNamed;
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
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

public class QueueReduction {

  private static final String MsgTagName = Designator.NAME_SEP + "msg";

  public static void process(Namespace classes, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);
    KnowPath kp = kb.getEntry(KnowPath.class);

    { // TODO keep queues of active components
      Queue queue = new Queue(Designator.NAME_SEP + Queue.DEFAULT_NAME);
      Map<Queue, Queue> map = new HashMap<Queue, Queue>();
      for (Queue old : ClassGetter.get(Queue.class, classes)) {
        map.put(old, queue);
      }
      Relinker.relink(classes, map);
      classes.add(queue);
    }

    List<MsgPush> pushes = ClassGetter.get(MsgPush.class, classes);

    // Find destination functions
    Map<Queue, Set<FunctionHeader>> queues = new HashMap<Queue, Set<FunctionHeader>>();
    Set<FunctionHeader> funcs = new HashSet<FunctionHeader>();
    for (MsgPush push : pushes) {
      Queue queue = (Queue) push.getQueue().getLink();
      Set<FunctionHeader> set = queues.get(queue);
      if (set == null) {
        set = new HashSet<FunctionHeader>();
        queues.put(queue, set);
      }
      FunctionHeader func = (FunctionHeader) push.getFunc().getLink();
      set.add(func);
      funcs.add(func);
    }

    // create msg content types
    Map<FunctionHeader, RecordType> funrec = new HashMap<FunctionHeader, RecordType>();
    for (FunctionHeader func : funcs) {
      Designator path = kp.get(func);
      assert (path.size() > 0);
      String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);

      RecordType rec = new RecordType(func.getInfo(), Designator.NAME_SEP + "msg" + Designator.NAME_SEP + name, new ArrayList<NamedElement>());

      for (FuncVariable arg : func.getParam()) {
        rec.getElement().add(new NamedElement(arg.getInfo(), arg.getName(), arg.getType().copy()));
      }

      funrec.put(func, rec);

      classes.add(rec);
    }

    Map<FunctionHeader, FuncPrivateVoid> pushfunc = new HashMap<FunctionHeader, FuncPrivateVoid>();

    for (Queue queue : queues.keySet()) {
      // create queue type (union of records)
      String prefix = queue.getName() + Designator.NAME_SEP;

      ElementInfo info = queue.getInfo();
      EnumType msgType = new EnumType(info, prefix + "msgid");
      Map<FunctionHeader, NamedElement> elements = new HashMap<FunctionHeader, NamedElement>();

      Map<FunctionHeader, EnumElement> funem = new HashMap<FunctionHeader, EnumElement>();

      for (FunctionHeader func : queues.get(queue)) {
        Designator path = kp.get(func);
        assert (path.size() > 0);
        String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);

        NamedElement elem = new NamedElement(info, name, new TypeRef(info, funrec.get(func)));
        elements.put(func, elem);

        EnumElement enumElem = new EnumElement(info, prefix + name);
        msgType.getElement().add(enumElem);

        funem.put(func, enumElem);
      }

      NamedElement tag = new NamedElement(info, MsgTagName, new TypeRef(info, msgType));
      UnionType uni = new UnionType(info, prefix + "queue", elements.values(), tag);

      classes.add(msgType);
      classes.add(uni);

      // create queue

      int queueSize = 20;  // FIXME remove magic
      // create queue variable
      ArrayType qat = new ArrayType(BigInteger.valueOf(queueSize), new TypeRef(info, uni));
      classes.add(qat);
      StateVariable qv = new StateVariable(info, prefix + "vdata", new TypeRef(info, qat), new ArrayValue(info, new ArrayList<Expression>()));
      classes.add(qv);

      StateVariable qh = new StateVariable(info, prefix + "vhead", new TypeRef(info, kbi.getRangeType(queueSize)), new Number(info, BigInteger.ZERO));
      classes.add(qh);

      StateVariable qc = new StateVariable(info, prefix + "vcount", new TypeRef(info, kbi.getRangeType(queueSize + 1)), new Number(info, BigInteger.ZERO));
      classes.add(qc);

      // function to return number of elements in queue
      FuncInputHandlerQuery sizefunc = new FuncInputHandlerQuery(info, prefix + "count", new ListOfNamed<FuncVariable>());
      sizefunc.setAttribute(FuncAttr.Public);
      sizefunc.setRet(qc.getType().copy());
      Block sfb = new Block(info);
      sfb.getStatements().add(new ReturnExpr(info, new Reference(info, qc)));
      sizefunc.setBody(sfb);
      classes.add(sizefunc);

      // create dispatch function

      FuncInputHandlerEvent dispatcher = new FuncInputHandlerEvent(info, prefix + "dispatch", new ListOfNamed<FuncVariable>());
      dispatcher.setAttribute(FuncAttr.Public);
      Block body = createDispatchBody(funem, funrec, elements, qv, qh, qc);
      dispatcher.setBody(body);
      classes.add(dispatcher);

      // create push functions

      for (FunctionHeader func : queues.get(queue)) {
        Designator path = kp.get(func);
        assert (path.size() > 0);
        String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);

        FuncPrivateVoid pfunc = new FuncPrivateVoid(info, Designator.NAME_SEP + "push" + Designator.NAME_SEP + name, new ListOfNamed<FuncVariable>(Copy.copy(func.getParam().getList())));

        pfunc.setBody(createPushBody(pfunc, qv, qh, qc, queueSize, uni, msgType, funem.get(func), elements.get(func)));

        classes.add(pfunc);
        pushfunc.put(func, pfunc);
      }

    }

    PushReplacer pr = new PushReplacer();
    pr.traverse(classes, pushfunc);
  }

  private static Block createPushBody(FuncPrivateVoid pfunc, StateVariable qv, StateVariable qh, StateVariable qc, int queueSize, UnionType uni, EnumType msgType, EnumElement enumElement, NamedElement namedElement) {
    ElementInfo info = pfunc.getInfo();

    Collection<IfOption> option = new ArrayList<IfOption>();

    Block pushbody = new Block(info);

    FuncVariable idx = new FuncVariable(info, "wridx", new TypeRef(info, qh.getType().getRef()));
    pushbody.getStatements().add(new VarDefStmt(info, idx));
    pushbody.getStatements().add(new Assignment(info, new Reference(info, idx), new Mod(info, new Plus(info, new Reference(info, qh), new Reference(info, qc)), new Number(info, BigInteger.valueOf(queueSize)))));

    Reference qir = new Reference(info, qv);
    qir.getOffset().add(new RefIndex(info, new Reference(info, idx)));
    qir.getOffset().add(new RefName(info, uni.getTag().getName()));
    pushbody.getStatements().add(new Assignment(info, qir, new Reference(info, enumElement)));

    for (FuncVariable arg : pfunc.getParam()) {
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

  private static Block createDispatchBody(Map<FunctionHeader, EnumElement> funem, Map<FunctionHeader, RecordType> funrec, Map<FunctionHeader, NamedElement> elements, StateVariable qdata, StateVariable qhead, StateVariable qsize) {
    ElementInfo info = new ElementInfo();

    Block body = new Block(info);

    List<CaseOpt> opt = new ArrayList<CaseOpt>();
    Reference ref = new Reference(info, qdata);
    ref.getOffset().add(new RefIndex(info, new Reference(info, qhead)));
    ref.getOffset().add(new RefName(info, MsgTagName));
    CaseStmt caseStmt = new CaseStmt(info, ref, opt, new Block(info));

    for (FunctionHeader func : funem.keySet()) {
      List<CaseOptEntry> value = new ArrayList<CaseOptEntry>();
      value.add(new CaseOptValue(info, new Reference(info, funem.get(func))));
      CaseOpt copt = new CaseOpt(info, value, new Block(info));

      NamedElement un = elements.get(func);
      RecordType rec = funrec.get(func);
      ArrayList<Expression> acarg = new ArrayList<Expression>();
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

    ArrayList<IfOption> option = new ArrayList<IfOption>();
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

class PushReplacer extends DefTraverser<Statement, Map<FunctionHeader, FuncPrivateVoid>> {

  @Override
  protected Statement visitBlock(Block obj, Map<FunctionHeader, FuncPrivateVoid> param) {
    for (int i = 0; i < obj.getStatements().size(); i++) {
      Statement stmt = visit(obj.getStatements().get(i), param);
      if (stmt != null) {
        obj.getStatements().set(i, stmt);
      }
    }
    return null;
  }

  @Override
  protected Statement visitMsgPush(MsgPush obj, Map<FunctionHeader, FuncPrivateVoid> param) {
    assert (param.containsKey(obj.getFunc().getLink()));

    Reference call = new Reference(obj.getInfo(), param.get(obj.getFunc().getLink()));
    call.getOffset().add(new RefCall(obj.getInfo(), obj.getData()));

    return new CallStmt(obj.getInfo(), call);
  }

}
