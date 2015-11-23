Feature: run to completion error detection
  As a developer
  I want the compiler to check the run to completion condition
  In order to detect errors early

Scenario: loop over a single file
  Given we have a file "con1.rzy" with the content:
    """
    Dummy = Component
      out : signal();
      inp : slot();
        
    elementary
      inp : slot()
        out();
      end
    end

    Con1 = Component
      inp : slot();
      out : signal();
        
    composition
      sub : Dummy;
      
      inp -> out;
      sub.out -> sub.inp;
    end

    """

  When I start rizzly with the file "con1.rzy"
  
  Then I expect an error code
  And stderr should contain "Violation of run to completion detected"


Scenario: loop over 2 files
  Given we have a file "con2.rzy" with the content:
    """
    Dummy = Component
      out : signal();
      inp : slot();
        
    elementary
      inp : slot()
        out();
      end
    end

    Con2 = Component
      inp : slot();
      out : signal();
        
    composition
      sub1 : Dummy;
      sub2 : Dummy;
      
      inp -> out;
      sub1.out -> sub2.inp;
      sub2.out -> sub1.inp;
    end

    """

  When I start rizzly with the file "con2.rzy"
  
  Then I expect an error code
  And stderr should contain "Violation of run to completion detected"

