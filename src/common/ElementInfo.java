package common;

import java.util.ArrayList;

public class ElementInfo {
  private String filename;
  private int line;
  private int row;
  final private ArrayList<Metadata> metadata = new ArrayList<Metadata>();

  public ElementInfo(String filename, int line, int row) {
    super();
    this.filename = filename;
    this.line = line;
    this.row = row;
  }

  public ElementInfo() {
    this.filename = "";
    this.line = 0;
    this.row = 0;
  }

  public String getFilename() {
    return filename;
  }

  public int getLine() {
    return line;
  }

  public int getRow() {
    return row;
  }

  public ArrayList<Metadata> getMetadata() {
    return metadata;
  }

  public ArrayList<Metadata> getMetadata( String filterKey ) {
    ArrayList<Metadata> ret = new ArrayList<Metadata>();
    for( Metadata itr : metadata ){
      if( itr.getKey().equals(filterKey) ){
        ret.add(itr);
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    return filename + ": " + line + "," + row;
  }

}
