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

package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import joGraph.HtmlGraphWriter;
import parser.FileParser;
import util.Pair;
import util.SimpleGraph;
import util.StreamWriter;

import common.Designator;
import common.ElementInfo;

import fun.Fun;
import fun.doc.DepGraph;
import fun.doc.DocWriter;
import fun.doc.FunPrinter;
import fun.expression.reference.Reference;
import fun.knowledge.KnowledgeBase;
import fun.other.FunList;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.other.SymbolTable;
import fun.other.Template;
import fun.traverser.CheckNames;
import fun.traverser.CompLinkReduction;
import fun.traverser.EnumLinkReduction;
import fun.traverser.Linker;
import fun.traverser.NamespaceLinkReduction;
import fun.traverser.StateLinkReduction;
import fun.traverser.spezializer.TypeEvalReplacer;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.VoidType;
import fun.type.template.ArrayTemplate;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeTemplate;
import fun.type.template.TypeTypeTemplate;
import fun.variable.CompUse;
import fun.variable.TemplateParameter;

public class MainFun {
  private static ElementInfo info = ElementInfo.NO;

  public static Pair<Namespace, CompUse> doFun(ClaOption opt, Designator rootfile, String debugdir, String docdir) {
    Namespace fileList = loadFiles(rootfile, opt.getRootPath());

    print(fileList, debugdir + "loaded.rzy");

    FunList<Fun> types = new FunList<Fun>();
    genPrimitiveTypes(types);
    genPrimitiveGenericTypes(types);

    CheckNames.check(fileList, types.names());

    SymbolTable sym = new SymbolTable();
    sym.addAll(types);
    Linker.process(types, fileList, sym);
    Linker.process(fileList, fileList, sym);
    // print(fileList, debugdir + "linked.rzy", new KnowledgeBase(fileList, new
    // HashSet<RizzlyFile>(), debugdir));

    Namespace classes = new Namespace(info, "!");
    classes.addAll(types);
    Collection<Pair<Designator, RizzlyFile>> files = new ArrayList<Pair<Designator, RizzlyFile>>();
    fileList.getItems(RizzlyFile.class, new Designator(), files);
    for (Pair<Designator, RizzlyFile> f : files) {
      Namespace parent = classes.forceChildPath(f.first.toList());
      parent.addAll(f.second.getObjects());
    }

    KnowledgeBase kb = new KnowledgeBase(classes, fileList, debugdir);

    print(classes, debugdir + "pretty.rzy");

    NamespaceLinkReduction.process(classes);
    StateLinkReduction.process(classes, kb);
    EnumLinkReduction.process(classes, kb);
    CompLinkReduction.process(classes);

    DocWriter.print(files, kb);

    Template rootdecl = (Template) classes.getChildItem(opt.getRootComp().toList());
    CompUse root = new CompUse(ElementInfo.NO, "inst", new Reference(ElementInfo.NO, rootdecl));
    classes.getChildren().add(root);

    print(classes, debugdir + "linkreduced.rzy");
    evaluate(classes, debugdir, fileList);
    print(classes, debugdir + "evaluated.rzy");

    // FIXME if we remove everything unused, we can not typecheck that in EVL
    removeUnused(classes, root, debugdir + "used.gv");
    print(classes, debugdir + "stripped.rzy");
    // printDepGraph(debugdir + "rdep.gv", classes);
    return new Pair<Namespace, CompUse>(classes, root);
  }

  private static void printDepGraph(String filename, SimpleGraph<Fun> g) {
    try {
      @SuppressWarnings("resource")
      HtmlGraphWriter<Fun, Pair<Fun, Fun>> writer = new HtmlGraphWriter<Fun, Pair<Fun, Fun>>(new joGraph.Writer(new PrintStream(filename))) {
        @Override
        protected void wrVertex(Fun v) {
          wrVertexStart(v);
          wrRow(v.toString());
          wrVertexEnd();
        }
      };
      writer.print(g);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void evaluate(Namespace oldClasses, String debugdir, Namespace fileList) {
    KnowledgeBase kb = new KnowledgeBase(oldClasses, fileList, debugdir);

    TypeEvalReplacer replacer = new TypeEvalReplacer(kb);
    replacer.traverse(oldClasses, null);
  }

  private static void removeUnused(Namespace classes, Fun root, String graphfile) {
    SimpleGraph<Fun> g = DepGraph.build(root);
    printDepGraph(graphfile, g);
    removeUnused(classes, g.vertexSet());
  }

  private static void removeUnused(Namespace ns, Set<Fun> keep) {
    Set<Fun> remove = new HashSet<Fun>();
    for (Fun itr : ns.getChildren()) {
      if (itr instanceof Namespace) {
        removeUnused((Namespace) itr, keep);
      } else if (!keep.contains(itr)) {
        remove.add(itr);
      }
    }
    ns.getChildren().removeAll(remove);
  }

  private static void inst(Type object, FunList<Fun> container) {
    container.add(object);
  }

  private static void templ(String name, FunList<TemplateParameter> list, TypeTemplate tmpl, FunList<Fun> container) {
    Template decl = new Template(tmpl.getInfo(), name, list, tmpl);
    container.add(decl);
  }

  private static void genPrimitiveTypes(FunList<Fun> container) {
    inst(new BooleanType(), container);
    inst(VoidType.INSTANCE, container);
    inst(new NaturalType(), container);
    inst(new IntegerType(), container);
    inst(new AnyType(), container);
    inst(new StringType(), container);
  }

  private static void genPrimitiveGenericTypes(FunList<Fun> container) {
    templ(RangeTemplate.NAME, RangeTemplate.makeParam(), new RangeTemplate(), container);
    templ(ArrayTemplate.NAME, ArrayTemplate.makeParam(), new ArrayTemplate(), container);
    templ(TypeTypeTemplate.NAME, TypeTypeTemplate.makeParam(), new TypeTypeTemplate(), container);
  }

  private static Namespace loadFiles(Designator rootname, String rootdir) {
    Namespace loaded = new Namespace(info, "!!");

    Queue<Designator> toload = new LinkedList<Designator>();
    toload.add(rootname);

    while (!toload.isEmpty()) {
      Designator lname = toload.poll();
      assert (lname.size() > 0);
      lname.sub(0, lname.size() - 1);
      Namespace parent = loaded.forceChildPath(lname.sub(0, lname.size() - 1).toList());
      if (parent.getChildren().find(lname.last()) != null) {
        continue;
      }
      String filename = rootdir + lname.toString(File.separator) + ClaOption.extension;
      RizzlyFile lfile = FileParser.parse(filename, lname.last());

      parent.getChildren().add(lfile);

      for (Designator name : lfile.getImports()) {
        toload.add(name);
      }
    }
    return loaded;
  }

  static private void print(Fun ast, String filename) {
    try {
      StreamWriter writer = new StreamWriter(new PrintStream(filename));
      FunPrinter pp = new FunPrinter(writer);
      pp.traverse(ast, null);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}
