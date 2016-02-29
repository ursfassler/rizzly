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

package main.pass;

import java.util.LinkedList;

import ast.meta.MetaListImplementation;
import error.ErrorType;
import error.RizzlyError;

public class PassArgumentParser {
  private final RizzlyError error;

  public PassArgumentParser(RizzlyError error) {
    this.error = error;
  }

  public LinkedList<String> parse(String value) {
    if (value.isEmpty()) {
      return new LinkedList<String>();
    }

    int nameEnd = value.indexOf('(');
    if (nameEnd < 0) {
      return parseWithoutArguments(value);
    } else {
      return parseWithArguments(value, nameEnd);
    }
  }

  private LinkedList<String> parseWithArguments(String value, int nameEnd) {
    LinkedList<String> list = new LinkedList<String>();
    list.add(value.substring(0, nameEnd).trim());
    int argEnd = value.indexOf(')', nameEnd);
    if (argEnd < 0) {
      error.err(ErrorType.Error, "Missing closing parentheses: " + value, new MetaListImplementation());
    }
    String argString = value.substring(nameEnd + 1, argEnd);
    list.addAll(parseArguments(argString.trim()));
    return list;
  }

  private LinkedList<String> parseWithoutArguments(String value) {
    LinkedList<String> list = new LinkedList<String>();
    list.add(value.trim());
    return list;
  }

  private LinkedList<String> parseArguments(String arguments) {
    LinkedList<String> list = new LinkedList<String>();

    if (!arguments.isEmpty()) {
      String[] args = arguments.split(",");
      for (String arg : args) {
        String trimmed = arg.trim();
        assert (!trimmed.isEmpty());
        trimmed = decodeArgument(trimmed);
        list.add(trimmed);
      }
    }

    return list;
  }

  private String decodeArgument(String trimmed) {
    if (trimmed.startsWith("'")) {
      assert (trimmed.endsWith("'"));
      trimmed = trimmed.substring(1, trimmed.length() - 1);
    }
    return trimmed;
  }
}
