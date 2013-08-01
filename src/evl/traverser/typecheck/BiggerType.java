package evl.traverser.typecheck;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.EnumType;

public class BiggerType {

  static public Type get(Type lhs, Type rhs, ElementInfo info, KnowledgeBase kb) {
    Type bigger;
    // TODO implement correct test, may depend on operator
    if ((lhs instanceof EnumType) || (rhs instanceof EnumType)) {
      RError.err(ErrorType.Error, info, "Arithmetic operation not possible on enumerator");
      bigger = null;
    } else if (LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
      bigger = lhs;
    } else if (LeftIsContainerOfRightTest.process(rhs, lhs, kb)) {
      bigger = rhs;
    } else {
      RError.err(ErrorType.Error, info, "Incompatible types: " + lhs.getName() + " <-> " + rhs.getName());
      bigger = null;
    }
    return bigger;
  }

}
