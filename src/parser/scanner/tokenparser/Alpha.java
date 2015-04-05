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

package parser.scanner.tokenparser;

import java.util.HashMap;
import java.util.Map;

import parser.PeekReader;
import parser.scanner.Symbol;
import parser.scanner.Token;
import parser.scanner.TokenType;

public class Alpha extends TokenParser {
  private static final Map<String, TokenType> keywords = Alpha.get();

  public Alpha(PeekReader<Symbol> reader) {
    super(reader);
  }

  @Override
  public Token parse() {
    Symbol pos = reader.peek();
    String id = readIdentifier();
    if (keywords.containsKey(id)) {
      return Helper.token(keywords.get(id), pos);
    } else {
      return Helper.token(TokenType.IDENTIFIER, id, pos);
    }
  }

  // EBNF id: alpha { alpha | numeric}
  private String readIdentifier() {
    String text = Character.toString(reader.next().sym);
    while (Helper.isAlphaNummeric(reader.peek().sym)) {
      text = text + reader.next().sym;
    }
    return text;
  }

  static private Map<String, TokenType> get() {
    Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    keywords.putAll(functionKeywords());
    keywords.putAll(typedeclKeywords());
    keywords.putAll(componentTypeKeywords());
    keywords.putAll(operatorKeywords());
    keywords.putAll(statementKeywords());
    keywords.putAll(fileKeywords());
    keywords.putAll(otherKeywords());
    return keywords;
  }

  // TODO split; get better name
  static private Map<String, TokenType> otherKeywords() {
    Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    keywords.put("state", TokenType.STATE);
    keywords.put("to", TokenType.TO);
    keywords.put("by", TokenType.BY);

    keywords.put("end", TokenType.END);

    keywords.put("const", TokenType.CONST);
    keywords.put("register", TokenType.REGISTER);

    keywords.put("is", TokenType.IS);
    keywords.put("as", TokenType.AS);
    keywords.put("in", TokenType.IN);

    keywords.put("False", TokenType.FALSE);
    keywords.put("True", TokenType.TRUE);
    return keywords;
  }

  static private Map<String, TokenType> fileKeywords() {
    Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    keywords.put("import", TokenType.IMPORT);
    return keywords;
  }

  static private Map<String, TokenType> statementKeywords() {
    Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    keywords.put("for", TokenType.FOR);
    keywords.put("while", TokenType.WHILE);
    keywords.put("case", TokenType.CASE);
    keywords.put("do", TokenType.DO);
    keywords.put("of", TokenType.OF);
    keywords.put("if", TokenType.IF);
    keywords.put("then", TokenType.THEN);
    keywords.put("ef", TokenType.EF);
    keywords.put("else", TokenType.ELSE);
    keywords.put("return", TokenType.RETURN);
    return keywords;
  }

  static private Map<String, TokenType> operatorKeywords() {
    Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    keywords.put("not", TokenType.NOT);
    keywords.put("xor", TokenType.XOR);
    keywords.put("or", TokenType.OR);
    keywords.put("mod", TokenType.MOD);
    keywords.put("and", TokenType.AND);
    keywords.put("shr", TokenType.SHR);
    keywords.put("shl", TokenType.SHL);
    return keywords;
  }

  static private Map<String, TokenType> componentTypeKeywords() {
    Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    keywords.put("elementary", TokenType.ELEMENTARY);
    keywords.put("composition", TokenType.COMPOSITION);
    keywords.put("hfsm", TokenType.HFSM);
    return keywords;
  }

  static private Map<String, TokenType> typedeclKeywords() {
    Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    keywords.put("Component", TokenType.COMPONENT);
    keywords.put("Record", TokenType.RECORD);
    keywords.put("Union", TokenType.UNION);
    keywords.put("Enum", TokenType.ENUM);
    return keywords;
  }

  static private Map<String, TokenType> functionKeywords() {
    Map<String, TokenType> ret = new HashMap<String, TokenType>();
    ret.put("function", TokenType.FUNCTION);
    ret.put("procedure", TokenType.PROCEDURE);
    ret.put("query", TokenType.QUERY);
    ret.put("response", TokenType.RESPONSE);
    ret.put("signal", TokenType.SIGNAL);
    ret.put("slot", TokenType.SLOT);
    ret.put("interrupt", TokenType.INTERRUPT);
    ret.put("entry", TokenType.ENTRY);
    ret.put("exit", TokenType.EXIT);
    return ret;
  }

}
