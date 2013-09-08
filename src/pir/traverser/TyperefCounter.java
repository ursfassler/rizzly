package pir.traverser;

import error.ErrorType;
import error.RError;
import java.util.HashMap;
import java.util.Map;

import pir.DefTraverser;
import pir.other.Program;
import pir.type.Type;
import pir.type.TypeRef;

public class TyperefCounter extends DefTraverser<Void, Void> {

  final private Map<Type, Integer> count = new HashMap<Type, Integer>();

  public static Map<Type, Integer> process(Program obj) {
    TyperefCounter changer = new TyperefCounter();
    changer.traverse(obj, null);
    return changer.count;
  }

  private void inc(Type type) {
    if( !this.count.containsKey(type) ) {
      RError.err(ErrorType.Fatal, "Reference to missing type: " + type.getName());
    }
    int count = this.count.get(type);
    count++;
    this.count.put(type, count);
  }

  @Override
  protected Void visitProgram(Program obj, Void param) {
    for( Type type : obj.getType() ) {
      assert ( !count.containsKey(type) );
      count.put(type, 0);
    }
    return super.visitProgram(obj, param);
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, Void param) {
    inc(obj.getRef());
    return super.visitTypeRef(obj, param);
  }
}
