Feature: Use arrays in Rizzly
  As a developer
  I want to use arrays
  In order to have basic data types


Scenario: Initialize an array with a function returning an array
  Given we have a file "arrayInit1.rzy" with the content:
    """
    arrayInit{N: Natural; T: Type{Any}} = function(init: T):Array{N, T}
      res : Array{N, T};
      i : R{0, N+1} = 0;
      
      while i < N do
        res[i] := init;
        i := R{0, N+1}( i + 1 );
      end
      
      return res;
    end

    ArrayInit1 = Component
    elementary
      aconfig : Array{10, R{0,20}} = arrayInit{10,R{0,20}}( 15 );
    end

    """

  When I start rizzly with the file "arrayInit1.rzy"
  
  Then I expect no error


Scenario: Initialize an array in the entry function
  Given we have a file "arrayTest.rzy" with the content:
    """
    ArrayTest = Component
      out : signal( x: R{0,200} );
      inp : slot( x: R{0,19} );

    elementary
      a : Array{20,R{0,200}} = (0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
      
      entry
        i : R{0,20} = 0;
        while i < 20 do
          a[R{0,19}(i)] := i*10;
          i    := R{0,20}( i+1 );
        end
      end

      inp : slot( x: R{0,19} )
        out(a[x]);
      end

    end

    """

  When I succesfully compile "arrayTest.rzy" with rizzly
  And fully compile everything
  And I initialize it
  And I send an event inp(0)
  And I send an event inp(5)
  And I send an event inp(19)
  
  Then I expect an event out(0)
  And I expect an event out(50)
  And I expect an event out(190)
  And I expect no more events


Scenario: Set array element with setter
  Given we have a file "arrayTest2.rzy" with the content:
    """
    ArrayTest2 = Component
      set : slot( i: R{0,19}; x: R{0,199} );
      get : response( i: R{0,19} ): R{0,199};

    elementary
      a : Array{20,R{0,199}} = (0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
      
      set : slot( i: R{0,19}; x: R{0,199} )
        a[i] := x;
      end

      get : response( i: R{0,19} ): R{0,199}
        return a[i];
      end
      
    end

    """

  When I succesfully compile "arrayTest2.rzy" with rizzly
  And fully compile everything
  And I initialize it
  And I send an event set(0, 129)
  And I send an event set(18, 100)
  And I send an event set(7, 98)
  
  Then I expect the request get(10) = 0
  And I expect the request get(0) = 129
  And I expect the request get(7) = 98
  And I expect the request get(18) = 100
  And I expect no more events


Scenario: Use default array initializer
  Given we have a file "arrayTest3.rzy" with the content:
    """
    ArrayTest3 = Component
      get : response( x: R{0,2} ):R{0,31};

    elementary
      a : Array{3,R{0,31}} = default{Array{3,R{0,31}}}();
      
      get : response( x: R{0,2} ):R{0,31}
        return a[x];
      end

    end

    """

  When I succesfully compile "arrayTest3.rzy" with rizzly
  And fully compile everything
  And I initialize it
  
  Then I expect the request get(0) = 0
  And I expect the request get(1) = 0
  And I expect the request get(2) = 0


Scenario: Copy array from state
  Given we have a file "copy1.rzy" with the content:
    """
    Copy1 = Component
    elementary
      a : Array{10,R{0,200}} = (0,0,0,0,0,0,0,0,0,0);

      foo = procedure()
        b : Array{10,R{0,200}} = a;
      end

    end

    """

  When I start rizzly with the file "copy1.rzy"
  
  Then I expect no error


Scenario: Copy array variable
  Given we have a file "copy2.rzy" with the content:
    """
    Copy2 = Component
    elementary
      foo = procedure()
        a, b : Array{10,R{0,200}};
        b[0] := 1;
        a := b;
      end
    end

    """

  When I start rizzly with the file "copy2.rzy"
  
  Then I expect no error


#TODO implement this
@fixme
Scenario: Copy an array to one with larger elements
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
      smaller : Array{2,R{0,200}} = [1, 2];
      
      entry
        larger  : Array{2,R{0,1000}} = smaller;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect no error

