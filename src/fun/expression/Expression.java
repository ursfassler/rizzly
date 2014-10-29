package fun.expression;

import common.ElementInfo;

import fun.FunBase;
import fun.other.ActualTemplateArgument;

/**
 * 
 * @author urs
 */
abstract public class Expression extends FunBase implements ActualTemplateArgument {

  public Expression(ElementInfo info) {
    super(info);
  }
}
