Feature: Addition operators have the same precedence and are evaluated from left to right
  As a developer
  I want to use implicit but unambigous defined defined order of operation for addition operatos
  In order to write small expressions which are always evaluated identically


@fixme
Scenario: or followed by addition is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{0,10}):R{0,100};

    elementary
      op : response(a, b, c: R{0,10}):R{0,100}
        return a or b + c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(10, 8, 12) = 22


@fixme
Scenario: addition followed by or is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{0,10}):R{0,100};

    elementary
      op : response(a, b, c: R{0,10}):R{0,100}
        return a + b or c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(12, 8, 10) = 30


@fixme
Scenario: or followed by subtraction is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{0,10}):R{-100,100};

    elementary
      op : response(a, b, c: R{0,10}):R{-100,100}
        return a or b - c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(10, 8, 12) = -2


@fixme
Scenario: subtraction followed by or is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a: R{10,20}; b, c: R{0,10}):R{-100,100};

    elementary
      op : response(a: R{10,20}; b, c: R{0,10}):R{-100,100}
        return a - b or c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(12, 8, 10) = 14



