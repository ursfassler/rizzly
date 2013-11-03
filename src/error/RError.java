package error;

import common.ElementInfo;

//TODO introduce enum with entry for every error
//TODO make errors language independent (see above)
//TODO don't directly throw errors, let user ask if an fatal error occurred in order to finish a check

/**
 * 
 * @author urs
 */
public class RError {

  public static void err(ErrorType type, String filename, int line, int col, String msg) {
    switch (type) {
    case Hint:
    case Warning: {
      System.err.println(RException.mktxt(type, filename, line, col, msg));
      break;
    }
    case Error:
    case Fatal: {
      throw new RException(type, filename, line, col, msg);
    }
    }
  }

  public static void err(ErrorType error, ElementInfo info, String string) {
    err(error, info.getFilename(), info.getLine(), info.getRow(), string);
  }

  public static void err(ErrorType error, String string) {
    err(error, "", -1, -1, string);
  }
}
