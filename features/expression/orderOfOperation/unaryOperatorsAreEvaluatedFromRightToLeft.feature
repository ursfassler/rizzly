Feature: Unary operators have the same precedence and are evaluated from right to left
  As a developer
  I want to use implicit but unambigous defined defined order of operation for unary operatos
  In order to write small expressions which are always evaluated identically


#TODO this should work
@fixme
Scenario: not followed by minus is evaluated from right to left
  Given we have a file "notMinus.rzy" with the content:
    """
    NotMinus = Component
      op : response(value: R{-7,0}):R{0,7};

    elementary
      op : response(value: R{-7,0}):R{0,7}
        return not - value;
      end
    end

    """

  When I succesfully compile "notMinus.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(-4) = 3


Scenario: minus followed by not is evaluated from right to left
  Given we have a file "minusNot.rzy" with the content:
    """
    MinusNot = Component
      op : response(value: R{0,7}):R{-7,0};

    elementary
      op : response(value: R{0,7}):R{-7,0}
        return - not value;
      end
    end

    """

  When I succesfully compile "minusNot.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(3) = -4

