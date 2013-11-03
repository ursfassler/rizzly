package parser;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;
import common.Metadata;

import error.ErrorType;
import error.RError;
import fun.other.RizzlyFile;
import fun.variable.ConstGlobal;

/**
 * 
 * @author urs
 */
public class FileParser extends BaseParser {

  static public RizzlyFile parse(String filename) {
    Scanner s = new Scanner(new FileReader(filename), filename);
    FileParser p = new FileParser(s);
    RizzlyFile file = p.parseFile();
    return file;
  }

  public FileParser(Scanner scanner) {
    super(scanner);
  }

  // ---- Parser Functions ----

  // EBNF file: import { ifacedefsec | compdefsec | typesec | constDeclBlock | globalFunction }
  private RizzlyFile parseFile() {
    ElementInfo info = peek().getInfo();
    ArrayList<Metadata> meta = getMetadata();
    info.getMetadata().addAll(meta);
    List<Designator> imp = parseImport();

    RizzlyFile ret = new RizzlyFile(info, imp);

    while (peek().getType() != TokenType.EOF) {
      switch (peek().getType()) {
      case COMPONENT: {
        ret.getCompfunc().addAll(type().parseComponentSection());
        break;
      }
      case TYPE_SEC: {
        ret.getCompfunc().addAll(type().parseTypeSection(ret.getConstant()));
        break;
      }
      case CONST: {
        ret.getConstant().addAll(type().parseConstDefBlock(ConstGlobal.class));
        break;
      }
      case FUNCTION: {
        ret.getCompfunc().add(type().parseGlobalFunction());
        break;
      }
      default: {
        Token got = peek();
        RError.err(ErrorType.Error, got.getInfo(), "got unexpected token: " + got);
        ret = null;
        break;
      }
      }
    }
    assert (ret != null);
    expect(TokenType.EOF);
    return ret;
  }

  // EBNF import: [ "import" designator ";" { designator ";" } ]
  private List<Designator> parseImport() {
    List<Designator> res = new ArrayList<Designator>();
    if (consumeIfEqual(TokenType.IMPORT)) {
      do {
        List<String> filename = parseDesignator();
        res.add(new Designator(filename));
        expect(TokenType.SEMI);
      } while (peek().getType() == TokenType.IDENTIFIER);
    }
    return res;
  }

}
