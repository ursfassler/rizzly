Feature: Multiplication operators have the same precedence and are evaluated from left to right
  As a developer
  I want to use implicit but unambigous defined defined order of operation for multiplication operatos
  In order to write small expressions which are always evaluated identically


@fixme
Scenario: multiplication followed by division is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{1,10}):R{0,100};

    elementary
      op : response(a, b, c: R{1,10}):R{0,100}
        return a * b / c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(5, 6, 10) = 3


@fixme
Scenario: division followed by multiplication is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{1,10}):R{0,100};

    elementary
      op : response(a, b, c: R{1,10}):R{0,100}
        return a / b * c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(10, 2, 3) = 15


@fixme
Scenario: multiplication followed by modulo is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{1,10}):R{0,100};

    elementary
      op : response(a, b, c: R{1,10}):R{0,100}
        return a * b mod c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(3, 6, 10) = 8


@fixme
Scenario: modulo followed by multiplication is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{1,10}):R{0,100};

    elementary
      op : response(a, b, c: R{1,10}):R{0,100}
        return a mod b * c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(7, 5, 3) = 6


@fixme
Scenario: multiplication followed by and is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{1,10}):R{0,100};

    elementary
      op : response(a, b, c: R{1,10}):R{0,100}
        return a * b and c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(3, 6, 7) = 2


@fixme
Scenario: and followed by multiplication is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{1,10}):R{0,100};

    elementary
      op : response(a, b, c: R{1,10}):R{0,100}
        return a and b * c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(7, 10, 3) = 6


@fixme
Scenario: multiplication followed by xor is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{0,7}):R{0,127};

    elementary
      op : response(a, b, c: R{0,7}):R{0,127}
        return a * b xor c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(5, 6, 7) = 25


@fixme
Scenario: xor followed by multiplication is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{1,7}):R{0,100};

    elementary
      op : response(a, b, c: R{1,7}):R{0,100}
        return a xor b * c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(7, 5, 6) = 12


@fixme
Scenario: division followed by shift left is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{0,3}):R{0,100};

    elementary
      op : response(a, b, c: R{0,3}):R{0,100}
        return a / b shl c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(3, 2, 1) = 2


@fixme
Scenario: division followed by shift right is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: R{0,100}):R{0,100};

    elementary
      op : response(a, b, c: R{0,100}):R{0,100}
        return a / b shr c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(24, 3, 1) = 4


@fixme
Scenario: logical and followed by xor is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: Boolean):Boolean;

    elementary
      op : response(a, b, c: Boolean):Boolean
        return a and b xor c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(False, False, True) = True


@fixme
Scenario: logical xor followed by and is evaluated from left to right
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b, c: Boolean):Boolean;

    elementary
      op : response(a, b, c: Boolean):Boolean
        return a xor b and c;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(True, False, False) = False

