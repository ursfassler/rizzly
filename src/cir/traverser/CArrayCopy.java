package cir.traverser;

import cir.knowledge.KnowType;
import cir.other.Program;
import cir.statement.Assignment;
import cir.statement.Statement;
import cir.type.ArrayType;
import cir.type.Type;

//TODO reimplement
public class CArrayCopy extends StmtReplacer<Void> {
  private Program prog;

  public CArrayCopy(Program prog) {
    super();
    this.prog = prog;
  }

  public static void process(Program obj) {
    CArrayCopy arrayCopy = new CArrayCopy(obj);
    arrayCopy.traverse(obj, null);
  }

  @Override
  protected Statement visitAssignment(Assignment obj, Void param) {
    Type type = KnowType.get(obj.getDst());

    if (type instanceof ArrayType) { // FIXME what with type alias?
      // FIXME make library function prototypes at a proper place

      assert (false);
      return null;
      // int size = TypeSizeGetter.get(type);
      //
      // LibFunction memcpy = prog.getLibFunc("string", "memcpy");
      // RefCall rcall = new RefCall();
      // rcall.getParameter().add(obj.getDst());
      // rcall.getParameter().add(obj.getSrc());
      // rcall.getParameter().add(new Number(BigInteger.valueOf(size)));
      // CallStmt call = new CallStmt(new Reference(memcpy, rcall));
      // return call;
    }

    return obj;
  }

}
