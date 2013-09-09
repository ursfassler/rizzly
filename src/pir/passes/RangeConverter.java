package pir.passes;

import java.util.ArrayList;
import java.util.List;

import pir.cfg.PhiStmt;
import pir.expression.Number;
import pir.expression.reference.VarRefSimple;
import pir.know.KnowBaseItem;
import pir.know.KnowledgeBase;
import pir.other.PirValue;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.CallAssignment;
import pir.statement.CallStmt;
import pir.statement.Relation;
import pir.statement.Statement;
import pir.statement.StoreStmt;
import pir.statement.convert.TypeCast;
import pir.traverser.StatementReplacer;
import pir.type.RangeType;
import pir.type.TypeRef;

import common.NameFactory;
import pir.type.PointerType;
import pir.type.Type;

/**
 * Extends and truncates types of variables used in an arithmetic operation, relation and assignments
 * 
 * @author urs
 * 
 */
public class RangeConverter extends StatementReplacer<Void> {

  private KnowBaseItem kbi;

  public RangeConverter(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static void process(Program obj, KnowledgeBase kb) {
    RangeConverter changer = new RangeConverter(kb);
    changer.traverse(obj, null);
  }

  @Override
  protected List<Statement> visitRelation(Relation obj, Void param) {
    RangeType lt = (RangeType) obj.getLeft().getType().getRef();
    RangeType rt = (RangeType) obj.getRight().getType().getRef();

    RangeType bt = RangeType.makeContainer(lt, rt);
    bt = kbi.getRangeType(bt.getLow(), bt.getHigh()); // add bt to program

    List<Statement> ret = new ArrayList<Statement>();

    obj.setLeft(replaceIfNeeded(obj.getLeft(), lt, bt, ret));
    obj.setRight(replaceIfNeeded(obj.getRight(), rt, bt, ret));

    ret.add(obj);

    return ret;
  }

  private boolean isNotRange(Type t1, Type t2) {
    if( !( t1 instanceof RangeType ) ) {
      //TODO implement it nicer
      assert ( t1 == t2 );
      return true;
    } else {
      assert ( t2 instanceof RangeType );
    }
    return false;
  }

  private PirValue replaceIfNeeded(PirValue val, RangeType valType, RangeType commonType, List<Statement> ret) {
    if( RangeType.isBigger(commonType, valType) ) {
      if( val instanceof Number ) { // a number is of any type
        ( (Number) val ).setType(new TypeRef(commonType));
      } else {
        SsaVariable lev = new SsaVariable(NameFactory.getNew(), new TypeRef(commonType));
        TypeCast lex = new TypeCast(lev, val);
        val = new VarRefSimple(lev);
        ret.add(lex);
      }
    }
    return val;
  }

  @Override
  protected List<Statement> visitArithmeticOp(ArithmeticOp obj, Void param) {
    RangeType lt = (RangeType) obj.getLeft().getType().getRef();
    RangeType rt = (RangeType) obj.getRight().getType().getRef();
    RangeType dt = (RangeType) obj.getVariable().getType().getRef();

    RangeType it = RangeType.makeContainer(lt, rt);
    RangeType bt = RangeType.makeContainer(it, dt);
    bt = kbi.getRangeType(bt.getLow(), bt.getHigh()); // add bt to program

    List<Statement> ret = new ArrayList<Statement>();

    obj.setLeft(replaceIfNeeded(obj.getLeft(), lt, bt, ret));
    obj.setRight(replaceIfNeeded(obj.getRight(), rt, bt, ret));

    ret.add(obj);

    if( RangeType.isBigger(bt, dt) ) {
      SsaVariable irv = new SsaVariable(NameFactory.getNew(), new TypeRef(bt));
      TypeCast rex = new TypeCast(obj.getVariable(), new VarRefSimple(irv));
      obj.setVariable(irv);
      ret.add(rex);
    }
    return ret;
  }

  @Override
  protected List<Statement> visitPhiStmt(PhiStmt obj, Void param) {
    return super.visitPhiStmt(obj, param); // TODO implement it
  }

  @Override
  protected List<Statement> visitCallAssignment(CallAssignment obj, Void param) {
    {
      Type st = obj.getRef().getRetType().getRef();
      Type dt = obj.getVariable().getType().getRef();
      if( isNotRange(st, dt) ) {
        return null;
      }
    }
    List<SsaVariable> argument = obj.getRef().getArgument();
    ArrayList<PirValue> parameter = obj.getParameter();

    List<Statement> ret = checkArg(argument, parameter);
    ret.add(obj);

    RangeType lt = (RangeType) obj.getRef().getRetType().getRef();
    RangeType dt = (RangeType) obj.getVariable().getType().getRef();

    assert ( RangeType.isBigger(dt, lt) || RangeType.isEqual(dt, lt) );

    if( RangeType.isBigger(dt, dt) ) {
      SsaVariable irv = new SsaVariable(NameFactory.getNew(), new TypeRef(dt));
      TypeCast rex = new TypeCast(obj.getVariable(), new VarRefSimple(irv));
      obj.setVariable(irv);
      ret.add(rex);
    }

    return ret;
  }

  @Override
  protected List<Statement> visitCallStmt(CallStmt obj, Void param) {
    List<SsaVariable> argument = obj.getRef().getArgument();
    ArrayList<PirValue> parameter = obj.getParameter();

    List<Statement> ret = checkArg(argument, parameter);
    ret.add(obj);

    return ret;
  }

  private List<Statement> checkArg(List<SsaVariable> argument, ArrayList<PirValue> parameter) {
    assert ( argument.size() == parameter.size() );

    List<Statement> ret = new ArrayList<Statement>();

    for( int i = 0; i < argument.size(); i++ ) {
      PirValue actArg = parameter.get(i);
      SsaVariable defArg = argument.get(i);

      RangeType lt = (RangeType) actArg.getType().getRef();
      RangeType dt = (RangeType) defArg.getType().getRef();

      assert ( RangeType.isBigger(dt, lt) || RangeType.isEqual(dt, lt) );

      actArg = replaceIfNeeded(actArg, lt, dt, ret);
      parameter.set(i, actArg);
    }

    return ret;
  }

  @Override
  protected List<Statement> visitAssignment(Assignment obj, Void param) {
    {
      Type st = obj.getSrc().getType().getRef();
      Type dt = obj.getVariable().getType().getRef();
      if( isNotRange(st, dt) ) {
        return null;
      }
    }

    RangeType lt = (RangeType) obj.getSrc().getType().getRef();
    RangeType dt = (RangeType) obj.getVariable().getType().getRef();

    RangeType bt = RangeType.makeContainer(lt, dt);
    bt = kbi.getRangeType(bt.getLow(), bt.getHigh()); // add bt to program

    List<Statement> ret = new ArrayList<Statement>();

    obj.setSrc(replaceIfNeeded(obj.getSrc(), lt, bt, ret));

    ret.add(obj);

    if( RangeType.isBigger(bt, dt) ) {
      SsaVariable irv = new SsaVariable(NameFactory.getNew(), new TypeRef(bt));
      TypeCast rex = new TypeCast(obj.getVariable(), new VarRefSimple(irv));
      obj.setVariable(irv);
      ret.add(rex);
    }

    return ret;
  }

  @Override
  protected List<Statement> visitStoreStmt(StoreStmt obj, Void param) {
    Type st = obj.getSrc().getType().getRef();
    Type dt = obj.getDst().getType().getRef();
    assert ( dt instanceof PointerType );
    dt = ( (PointerType) dt ).getType().getRef();
    if( isNotRange(st, dt) ) {
      return null;
    }

    RangeType lrt = (RangeType) st;
    RangeType drt = (RangeType) dt;

    RangeType bt = RangeType.makeContainer(lrt, drt);
    bt = kbi.getRangeType(bt.getLow(), bt.getHigh()); // add bt to program

    List<Statement> ret = new ArrayList<Statement>();

    obj.setSrc(replaceIfNeeded(obj.getSrc(), lrt, bt, ret));

    ret.add(obj);

    return ret;
  }
}
