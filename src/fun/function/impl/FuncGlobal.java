package fun.function.impl;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.function.FuncWithBody;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.other.Generator;
import fun.other.ListOfNamed;
import fun.statement.Block;
import fun.variable.FuncVariable;
import fun.variable.TemplateParameter;

/**
 * Globally defined function. It is a pure function and can be executed at compile time.
 * 
 * @author urs
 */
public class FuncGlobal extends FunctionHeader implements Generator, FuncWithBody, FuncWithReturn {
  final private ListOfNamed<TemplateParameter> param = new ListOfNamed<TemplateParameter>();
  private Reference ret;
  private Block body;

  public FuncGlobal(ElementInfo info) {
    super(info);
  }

  @Override
  public Reference getRet() {
    return ret;
  }

  @Override
  public void setRet(Reference ret) {
    this.ret = ret;
  }

  @Override
  public Block getBody() {
    return body;
  }

  @Override
  public void setBody(Block body) {
    this.body = body;
  }

  @Override
  public ListOfNamed<TemplateParameter> getTemplateParam() {
    return param;
  }

  @Override
  public String toString() {
    String ret = getName();

    {
      ret += "{";
      boolean first = true;
      for (TemplateParameter tp : getTemplateParam()) {
        if (first) {
          first = false;
        } else {
          ret += "; ";
        }
        ret += tp.toString();
      }
      ret += "}";
    }

    {
      ret += "(";
      boolean first = true;
      for (FuncVariable tp : getParam()) {
        if (first) {
          first = false;
        } else {
          ret += "; ";
        }
        ret += tp.toString();
      }
      ret += ")";
    }

    return ret;
  }

}
