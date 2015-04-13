/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package parser;

import java.util.ArrayList;
import java.util.List;

import parser.scanner.FileReader;
import parser.scanner.Scanner;
import parser.scanner.Token;
import parser.scanner.TokenType;
import util.Pair;
import ast.Designator;
import ast.ElementInfo;
import ast.data.Ast;
import ast.data.Metadata;
import ast.data.file.RizzlyFile;
import ast.data.template.Template;
import ast.data.variable.ConstGlobal;
import ast.data.variable.TemplateParameter;
import error.ErrorType;
import error.RError;

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

  // EBNF file: import { ifacedefsec | compdefsec | typesec | constDeclBlock |
  // globalFunction }
  private RizzlyFile parseFile(String filename, String name) {
    ElementInfo info = peek().getInfo();
    ArrayList<Metadata> meta = getMetadata();
    info.metadata.addAll(meta);
    List<Designator> imp = parseImport();

    RizzlyFile ret = new RizzlyFile(info, name, imp);

    while (peek().getType() == TokenType.IDENTIFIER) {
      Pair<Token, List<TemplateParameter>> def = parseObjDef();

      if (consumeIfEqual(TokenType.EQUAL)) {
        Ast object = parseDeclaration(def.first.getData());
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

  private Ast parseDeclaration(String name) {
    switch (peek().getType()) {
      case FUNCTION: {
        return parseFuncDef(TokenType.FUNCTION, name, false);
      }
      case COMPONENT: {
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
