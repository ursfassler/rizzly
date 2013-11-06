package fun.other;

import fun.variable.TemplateParameter;

/**
 * 
 * @author urs
 */
public interface Generator extends Named {
  public ListOfNamed<TemplateParameter> getTemplateParam();
}
