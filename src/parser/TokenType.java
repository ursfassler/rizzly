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

//TODO cleanup, remove unused tokens
public enum TokenType {
  ERROR, STAR, DIV, PLUS, MINUS, EQUAL, NEQ, LOWER, GEQ, LEQ, GREATER, PERIOD, RANGE, COMMA, OPENPAREN, CLOSEPAREN, OPENBRACKETS, CLOSEBRACKETS, BECOMES, THEN, DO, OF, NUMBER, IDENTIFIER, SEMI, ELSE, IF, EF, WHILE, CASE, FOR, RETURN, EOF, IGNORE, COLON, OR, CONST, FUNCTION, PROCEDURE, END, NOT, MOD, AND, COMPONENT, ELEMENTARY, COMPOSITION, HFSM, RECORD, UNION, ENUM, IMPORT, QUERY, RESPONSE, SIGNAL, SLOT, INTERRUPT, SYNC_MSG, ASYNC_MSG, OPENCURLY, CLOSECURLY, SHR, SHL, FALSE, TRUE, STRING, STATE, TO, BY, ENTRY, EXIT, IS, AS, IN, REGISTER

}
