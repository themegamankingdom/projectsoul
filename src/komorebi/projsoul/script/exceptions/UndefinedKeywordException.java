package komorebi.projsoul.script.exceptions;

/**
 * 
 *
 * @author Andrew Faulkenberry
 */
public class UndefinedKeywordException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  public UndefinedKeywordException(String className)
  {
    super("Class " + className + " must define the static method \n" + 
                                            "\tpublic static String keyword()");
  }
}
