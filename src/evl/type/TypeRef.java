package evl.type;

import common.ElementInfo;

import evl.EvlBase;

public class TypeRef extends EvlBase {
  private Type ref;

  public TypeRef(ElementInfo info, Type ref) {
    super(info);
    this.ref = ref;
  }

  public Type getRef() {
    return ref;
  }

  public void setRef(Type ref) {
    this.ref = ref;
  }
  
  public TypeRef copy(){
    return new TypeRef(getInfo(), ref);
  }

}
