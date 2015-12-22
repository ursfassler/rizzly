Feature: throw an error when an uninitialzed variable is used
  As a developer
  I want the compiler to throw an error when I use an uninitialzed variable
  In order find errors early


#TODO implement
@fixme
Scenario: throw an error when a uninitialzed variable is used
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response():R{0,100};
    
    elementary
      inp: response():R{0,100}
        a : R{0,100};
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:7:12: Error: TODO error message"


Scenario: throw no error when the variable is initialized
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response():R{0,100};
    
    elementary
      inp: response():R{0,100}
        a : R{0,100};
        a := 42;
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO implement
@fixme
Scenario: throw an error when the variable is only initialized in the then branch of an if
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response(x: Boolean):R{0,100};
    
    elementary
      inp: response(x: Boolean):R{0,100}
        a : R{0,100};
        if x then
          a := 42;
        end
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:10:12: Error: TODO error message"


#TODO implement
@fixme
Scenario: throw an error when the variable is only initialized in the else branch of an if
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response(x: Boolean):R{0,100};
    
    elementary
      inp: response(x: Boolean):R{0,100}
        a : R{0,100};
        if x then
        else
          a := 42;
        end
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:11:12: Error: TODO error message"


Scenario: throw no error when the variable is initialized in the then and else branch of an if
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response(x: Boolean):R{0,100};
    
    elementary
      inp: response(x: Boolean):R{0,100}
        a : R{0,100};
        if x then
          a := 57;
        else
          a := 42;
        end
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: throw no error when the variable is initialized in every branch of an if else-if
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response(x: R{0,9}):R{0,100};
    
    elementary
      inp: response(x: R{0,9}):R{0,100}
        a : R{0,100};
        if x < 3 then
          a := 23;
        ef x < 8 then
          a := 42;
        else
          a := 57;
        end
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: throw no error when the uninitialized variable is not reachable
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response(x: Boolean):R{0,100};
    
    elementary
      inp: response(x: Boolean):R{0,100}
        a : R{0,100};
        if x then
          return 42;
        else
          a := 57;
        end
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO implement
@fixme
Scenario: throw an error when the variable is only initialized in some branches of a case
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response(x: R{0,9}):R{0,100};
    
    elementary
      inp: response(x: R{0,9}):R{0,100}
        a : R{0,100};
        case x of
          1:
            a := 23; 
          end
          4:
            a := 42;
          end
        end
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:11:12: Error: TODO error message"


Scenario: throw no error when the variable is initialized in the else branch of a case
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response(x: R{0,9}):R{0,100};
    
    elementary
      inp: response(x: R{0,9}):R{0,100}
        a : R{0,100};
        case x of
          1:
            a := 23; 
          end
          else
            a := 42;
          end
        end
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: throw no error when the variable is initialized every branch of a case and all possibilities are covered with branches
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: response(x: R{0,9}):R{0,100};
    
    elementary
      inp: response(x: R{0,9}):R{0,100}
        a : R{0,100};
        case x of
          1, 7, 9:
            a := 23; 
          end
          2..6:
            a := 42;
          end
          0, 8:
            a := 57;
          end
        end
        return a;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error



