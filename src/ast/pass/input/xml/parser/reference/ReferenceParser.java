package ast.pass.input.xml.parser.reference;

import java.util.Collection;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.reference.Anchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefItem;
import ast.data.reference.Reference;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.parser.Names;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class ReferenceParser implements Parser {
  private static final String Name = "Reference";
  private final ExpectionParser stream;
  private final XmlParser parser;
  private final RizzlyError error;

  public ReferenceParser(ExpectionParser stream, XmlParser parser, RizzlyError error) {
    this.stream = stream;
    this.parser = parser;
    this.error = error;
  }

  @Override
  public Collection<String> names() {
    return Names.list(Name);
  }

  @Override
  public Class<? extends Ast> type() {
    return Reference.class;
  }

  @Override
  public OffsetReference parse() {
    stream.elementStart(Name);
    Anchor anchor = parser.itemOf(Anchor.class);
    AstList<RefItem> offset = parser.itemsOf(RefItem.class);
    stream.elementEnd();

    return new OffsetReference(anchor, offset);
  }

}
