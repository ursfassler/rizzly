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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import joGraph.HtmlGraphWriter;
import pass.FunPass;
import util.Pair;
import util.SimpleGraph;
import util.StreamWriter;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.doc.FunPrinter;
import fun.knowledge.KnowledgeBase;
import fun.other.Namespace;
import fun.pass.CheckNames;
import fun.pass.CompLinkReduction;
import fun.pass.DocWriter;
import fun.pass.EnumLinkReduction;
import fun.pass.FileLoader;
import fun.pass.FileReduction;
import fun.pass.InternTypeAdder;
import fun.pass.Linker;
import fun.pass.NamespaceLinkReduction;
import fun.pass.RootInstanceAdder;
import fun.pass.StateLinkReduction;
import fun.pass.UnusedRemover;
import fun.pass.VarDefSplitter;
import fun.traverser.spezializer.TypeEvalReplacerPass;

public class MainFun {
  public static Namespace doFun(ClaOption opt, String debugdir) {
    List<Class<? extends FunPass>> passes = new ArrayList<Class<? extends FunPass>>();

    passes.add(FileLoader.class);

    passes.add(InternTypeAdder.class);
    passes.add(CheckNames.class);
    passes.add(Linker.class);
    if (opt.getDocOutput()) {
      passes.add(DocWriter.class);
    }
    passes.add(FileReduction.class);

    passes.add(NamespaceLinkReduction.class);
    passes.add(StateLinkReduction.class);
    passes.add(EnumLinkReduction.class);
    passes.add(CompLinkReduction.class);

    passes.add(RootInstanceAdder.class);

    passes.add(TypeEvalReplacerPass.class);
    passes.add(VarDefSplitter.class);
    passes.add(UnusedRemover.class);

    Namespace classes = new Namespace(ElementInfo.NO, "!");
    KnowledgeBase kb = new KnowledgeBase(classes, debugdir, opt);
    process(passes, classes, kb);

    return classes;
  }

  private static void process(List<Class<? extends FunPass>> passes, Namespace root, KnowledgeBase kb) {
    for (Class<? extends FunPass> ecl : passes) {
      FunPass pass = null;
      try {
        pass = ecl.newInstance();
      } catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (pass == null) {
        RError.err(ErrorType.Fatal, "Could not create pass: " + ecl.getName());
      } else {
        pass.process(root, kb);

        print(root, kb.getDebugDir() + pass.getName() + ".rzy");

        selfCheck(root, kb);
      }
    }
  }

  private static void selfCheck(Namespace root, KnowledgeBase kb) {
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
