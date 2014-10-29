package fun.variable;

import common.ElementInfo;

import fun.content.FileContent;
import fun.expression.Expression;
import fun.expression.reference.Reference;

final public class ConstGlobal extends Constant implements FileContent {
  public ConstGlobal(ElementInfo info, String name, Reference type, Expression def) {
    super(info, name, type, def);
  }

}
