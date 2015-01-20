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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pass.EvlPass;
import debug.DebugPrinter;
import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.hfsm.reduction.HfsmReduction;
import evl.hfsm.reduction.HfsmToFsm;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.Namespace;
import evl.pass.BitLogicCategorizer;
import evl.pass.BitnotFixer;
import evl.pass.CompareReplacer;
import evl.pass.CompositionReduction;
import evl.pass.ConstantPropagation;
import evl.pass.EnumReduction;
import evl.pass.HeaderWriter;
import evl.pass.IfCutter;
import evl.pass.InitVarTyper;
import evl.pass.Instantiation;
import evl.pass.IntroduceConvert;
import evl.pass.KnowledgeInvalidator;
import evl.pass.OpenReplace;
import evl.pass.RangeConverter;
import evl.pass.ReduceUnion;
import evl.pass.RemoveUnused;
import evl.pass.check.CompInterfaceTypeChecker;
import evl.pass.check.HfsmTransScopeCheck;
import evl.pass.check.Io;
import evl.pass.check.ModelChecker;
import evl.pass.check.Root;
import evl.pass.check.RtcViolation;
import evl.pass.check.Usefullness;
import evl.pass.check.type.TypeChecker;
import evl.pass.infrastructure.LinkOk;
import evl.traverser.ConstTyper;
import evl.traverser.SystemIfaceAdder;
import evl.traverser.debug.CompCascadeDepth;
import evl.traverser.debug.DebugIfaceAdder;
import evl.traverser.debug.MsgNamesGetter;
import evl.type.base.ArrayType;
import evl.type.base.RangeType;
import evl.type.special.VoidType;

//TODO ensure that composition and hfsm use construct and destruct correctly

public class MainEvl {
  public static void doEvl(ClaOption opt, String outdir, String debugdir, CompUse rootUse, Namespace aclasses) {
    KnowledgeBase kb = new KnowledgeBase(aclasses, rootUse, outdir, debugdir);
    DebugPrinter dp = new DebugPrinter(aclasses, debugdir);

    assert (aclasses.getChildren().contains(rootUse));
    // aclasses.getChildren().remove(rootUse);

    List<Class<? extends EvlPass>> passes = new ArrayList<Class<? extends EvlPass>>();

    passes.add(ConstTyper.class);

    if (!opt.doLazyModelCheck()) {
      passes.add(Root.class);
      passes.add(Usefullness.class);
      passes.add(RtcViolation.class);
      passes.add(Io.class);
      passes.add(HfsmTransScopeCheck.class);
      passes.add(CompInterfaceTypeChecker.class);
      passes.add(ModelChecker.class);
    }

    // FuncHeaderReplacegr.process(aclasses, kb); //TODO remove if not used

    passes.add(IntroduceConvert.class);
    passes.add(OpenReplace.class);
    passes.add(CompositionReduction.class);
    passes.add(HfsmToFsm.class);
    passes.add(HfsmReduction.class);
    passes.add(ReduceUnion.class);
    passes.add(InitVarTyper.class);
    passes.add(BitLogicCategorizer.class);
    passes.add(TypeChecker.class);
    passes.add(SystemIfaceAdder.class);
    // passes.add(ExprCutter.class); // TODO reimplement
    if (opt.doDebugEvent()) {
      // TODO reimplement
      // names.addAll(addDebug(aclasses, root, dp, kb));
      RError.err(ErrorType.Fatal, "Debug currently not supported");
      // only for debugging
      // typecheck(classes, debugdir);
    }

    passes.add(RangeConverter.class);
    passes.add(CompareReplacer.class);

    passes.add(BitnotFixer.class);

    passes.add(KnowledgeInvalidator.class);

    passes.add(Instantiation.class);

    passes.add(HeaderWriter.class);

    passes.add(ConstantPropagation.class);

    // Have to do it here since queue reduction creates enums
    passes.add(EnumReduction.class);
    passes.add(ConstantPropagation.class);

    passes.add(RemoveUnused.class);
    passes.add(KnowledgeInvalidator.class);

    passes.add(IfCutter.class);

    process(passes, aclasses, kb, dp);
  }

  private static void process(List<Class<? extends EvlPass>> passes, Namespace evl, KnowledgeBase kb, DebugPrinter dp) {
    for (Class<? extends EvlPass> ecl : passes) {
      EvlPass pass = null;
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
        pass.process(evl, kb);
        dp.print(pass.getName());

        selfCheck(evl, kb);
      }
    }
  }

  private static void selfCheck(Namespace evl, KnowledgeBase kb) {
    (new LinkOk()).process(evl, kb);
  }

  @SuppressWarnings("unused")
  private static void print(Map<Evl, Boolean> writes, Map<Evl, Boolean> reads, Map<Evl, Boolean> outputs, Map<Evl, Boolean> inputs) {
    for (Evl header : writes.keySet()) {
      String rwio = "";
      rwio += reads.get(header) ? "r" : " ";
      rwio += writes.get(header) ? "w" : " ";
      rwio += inputs.get(header) ? "i" : " ";
      rwio += outputs.get(header) ? "o" : " ";
      System.out.print(rwio);
      System.out.print("\t");
      System.out.print(header);
      System.out.println();
    }
  }

  private static ArrayList<String> addDebug(Namespace classes, Component root, DebugPrinter dp, KnowledgeBase kb) {
    ArrayList<String> names = new ArrayList<String>(MsgNamesGetter.get(classes));
    if (names.isEmpty()) {
      return names; // this means that there is no input nor output interface
    }

    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    int depth = CompCascadeDepth.get(root);
    depth += 2;
    Collections.sort(names);

    RangeType symNameSizeType = kbi.getRangeType(names.size());
    ArrayType arrayType = kbi.getArray(BigInteger.valueOf(depth), symNameSizeType);
    RangeType sizeType = kbi.getRangeType(depth);
    VoidType voidType = kbi.getVoidType();

    DebugIfaceAdder.process(classes, arrayType, sizeType, symNameSizeType, voidType, names);

    dp.print("debug");

    return names;
  }

}
