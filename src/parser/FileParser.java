package parser;

import java.util.ArrayList;
import java.util.List;

import util.Pair;

import common.Designator;
import common.ElementInfo;
import common.Metadata;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.other.RizzlyFile;
import fun.other.Template;
import fun.variable.ConstGlobal;
import fun.variable.TemplateParameter;

/**
 * 
 * @author urs
 */
public class FileParser extends BaseParser {

  static public RizzlyFile parse(String filename, String name) {
    Scanner s = new Scanner(new FileReader(filename), filename);
    FileParser p = new FileParser(s);
    RizzlyFile file = p.parseFile(filename, name);
    return file;
  }

  public FileParser(Scanner scanner) {
    super(scanner);
  }

  // ---- Parser Functions ----

  // EBNF file: import { ifacedefsec | compdefsec | typesec | constDeclBlock | globalFunction }
  private RizzlyFile parseFile(String filename, String name) {
    ElementInfo info = peek().getInfo();
    ArrayList<Metadata> meta = getMetadata();
    info.getMetadata().addAll(meta);
    List<Designator> imp = parseImport();

    RizzlyFile ret = new RizzlyFile(info, name, imp);

    while (peek().getType() == TokenType.IDENTIFIER) {
      Pair<Token, List<TemplateParameter>> def = parseObjDef();

      if (consumeIfEqual(TokenType.EQUAL)) {
        Fun object = parseDeclaration(def.first.getData());
        Template decl = new Template(def.first.getInfo(), def.first.getData(), def.second, object);
        ret.getObjects().add(decl);
      } else if (consumeIfEqual(TokenType.COLON)) {
        if (!def.second.isEmpty()) {
          RError.err(ErrorType.Error, def.second.get(0).getInfo(), "no generic arguments allowed for instantiations");
        }
        ConstGlobal object = type().parseConstDef(ConstGlobal.class, def.first.getData());
        expect(TokenType.SEMI);

        ret.getObjects().add(object);
      } else {
        Token got = peek();
        RError.err(ErrorType.Error, got.getInfo(), "got unexpected token: " + got);
        ret = null;
        break;
      }
    }
    expect(TokenType.EOF);
    return ret;
  }

  private Fun parseDeclaration(String name) {
    switch (peek().getType()) {
      case FUNCTION: {
        return parseFuncDef(TokenType.FUNCTION, name, false);
      }
      case ELEMENTARY:
      case HFSM:
      case COMPOSITION: {
        return type().parseCompdecl(name);
      }
      default: {
        return type().parseTypeDef(name);
      }
    }
  }

  // EBNF import: { "import" designator ";" }
  private List<Designator> parseImport() {
    List<Designator> res = new ArrayList<Designator>();
    while (consumeIfEqual(TokenType.IMPORT)) {
      List<String> filename = parseDesignator();
      res.add(new Designator(filename));
      expect(TokenType.SEMI);
    }
    return res;
  }

}
