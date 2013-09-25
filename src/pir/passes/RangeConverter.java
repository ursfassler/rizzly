package pir.passes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import pir.NullTraverser;
import pir.PirObject;
import pir.expression.Number;
import pir.expression.reference.VarRefSimple;
import pir.know.KnowBaseItem;
import pir.know.KnowledgeBase;
import pir.other.PirValue;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.statement.normal.Assignment;
import pir.statement.normal.CallAssignment;
import pir.statement.normal.CallStmt;
import pir.statement.normal.GetElementPtr;
import pir.statement.normal.NormalStmt;
import pir.statement.normal.StoreStmt;
import pir.statement.normal.binop.Arithmetic;
import pir.statement.normal.binop.Relation;
import pir.statement.normal.convert.TypeCast;
import pir.statement.phi.PhiStmt;
import pir.traverser.StatementReplacer;
import pir.type.ArrayType;
import pir.type.PointerType;
import pir.type.RangeType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeRef;
import pir.type.UnionType;

import common.NameFactory;

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
  protected List<NormalStmt> visitArithmetic(Arithmetic obj, Void param) {
    RangeType lt = (RangeType) obj.getLeft().getType().getRef();
    RangeType rt = (RangeType) obj.getRight().getType().getRef();
    RangeType dt = (RangeType) obj.getVariable().getType().getRef();

    RangeType it = RangeType.makeContainer(lt, rt);
    RangeType bt = RangeType.makeContainer(it, dt);
    bt = kbi.getRangeType(bt.getLow(), bt.getHigh()); // add bt to program

    List<NormalStmt> ret = new ArrayList<NormalStmt>();

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
  protected List<NormalStmt> visitRelation(Relation obj, Void param) {
    RangeType lt = (RangeType) obj.getLeft().getType().getRef();
    RangeType rt = (RangeType) obj.getRight().getType().getRef();
    RangeType bt = RangeType.makeContainer(lt, rt);
    bt = kbi.getRangeType(bt.getLow(), bt.getHigh()); // add bt to program

    List<NormalStmt> ret = new ArrayList<NormalStmt>();

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

  private PirValue replaceIfNeeded(PirValue val, RangeType valType, RangeType commonType, List<NormalStmt> ret) {
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
  protected List<NormalStmt> visitPhiStmt(PhiStmt obj, Void param) {
    return super.visitPhiStmt(obj, param); // TODO implement it
  }

  @Override
  protected List<NormalStmt> visitCallAssignment(CallAssignment obj, Void param) {
    List<SsaVariable> argument = obj.getRef().getArgument();
    ArrayList<PirValue> parameter = obj.getParameter();

    List<NormalStmt> ret = checkArg(argument, parameter);
    ret.add(obj);

    {
      Type st = obj.getRef().getRetType().getRef();
      Type dt = obj.getVariable().getType().getRef();
      if( isNotRange(st, dt) ) {
        return ret;
      }
    }
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
  protected List<NormalStmt> visitCallStmt(CallStmt obj, Void param) {
    List<SsaVariable> argument = obj.getRef().getArgument();
    ArrayList<PirValue> parameter = obj.getParameter();

    List<NormalStmt> ret = checkArg(argument, parameter);
    ret.add(obj);

    return ret;
  }

  private List<NormalStmt> checkArg(List<SsaVariable> argument, ArrayList<PirValue> parameter) {
    assert ( argument.size() == parameter.size() );

    List<NormalStmt> ret = new ArrayList<NormalStmt>();

    for( int i = 0; i < argument.size(); i++ ) {
      PirValue actArg = parameter.get(i);
      SsaVariable defArg = argument.get(i);

      Type st = actArg.getType().getRef();
      Type pt = defArg.getType().getRef();
      if( isNotRange(st, pt) ) {
        continue;
      }

      RangeType lt = (RangeType) st;
      RangeType dt = (RangeType) pt;

      assert ( RangeType.isBigger(dt, lt) || RangeType.isEqual(dt, lt) );

      actArg = replaceIfNeeded(actArg, lt, dt, ret);
      parameter.set(i, actArg);
    }

    return ret;
  }

  @Override
  protected List<NormalStmt> visitAssignment(Assignment obj, Void param) {
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

    List<NormalStmt> ret = new ArrayList<NormalStmt>();

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
  protected List<NormalStmt> visitStoreStmt(StoreStmt obj, Void param) {
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

    List<NormalStmt> ret = new ArrayList<NormalStmt>();

    obj.setSrc(replaceIfNeeded(obj.getSrc(), lrt, bt, ret));

    ret.add(obj);

    return ret;
  }

  @Override
  protected List<NormalStmt> visitGetElementPtr(GetElementPtr obj, Void param) {
    List<NormalStmt> ret = new ArrayList<NormalStmt>();

    ChildType extender = new ChildType(ret);
    Type type = obj.getBase().getType().getRef();
    for( int i = 0; i < obj.getOffset().size(); i++ ) {
      PirValue val = obj.getOffset().get(i);
      if( type instanceof ArrayType ) {
        // extend array indices. We need a big enough data type as index, otherwise strange things may happen.
        RangeType valType = (RangeType) val.getType().getRef();
        RangeType arrType = (RangeType) ( (ArrayType) type ).getType().getRef();
        val = replaceIfNeeded(val, valType, arrType, ret);
        obj.getOffset().set(i, val);
      }
      type = extender.traverse(type, val);
    }

    ret.add(obj);
    return ret;
//    return super.visitGetElementPtr(obj, param);
  }
}

class ChildType extends NullTraverser<Type, PirValue> {

  private List<NormalStmt> ret;

  public ChildType(List<NormalStmt> ret) {
    this.ret = ret;
  }

  @Override
  protected Type doDefault(PirObject obj, PirValue param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitArray(ArrayType obj, PirValue param) {
    return obj.getType().getRef();
  }

  @Override
  protected Type visitPointerType(PointerType obj, PirValue param) {
    assert ( param instanceof Number );
    assert ( ( (Number) param ).getValue() == BigInteger.ZERO );
    return obj.getType().getRef();
  }

  @Override
  protected Type visitStructType(StructType obj, PirValue param) {
    assert ( param instanceof Number );
    int idx = ( (Number) param ).getValue().intValue();
    assert ( idx >= 0 );
    assert ( idx < obj.getElements().size() );
    return obj.getElements().get(idx).getType().getRef();
  }

  @Override
  protected Type visitUnionType(UnionType obj, PirValue param) {
    assert ( param instanceof Number );
    int idx = ( (Number) param ).getValue().intValue();
    assert ( idx >= 0 );
    assert ( idx < obj.getElements().size() );
    return obj.getElements().get(idx).getType().getRef();
  }
}
