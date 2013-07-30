package metadata.parser;

import java.util.LinkedList;
import java.util.List;

import parser.PeekReader;

import common.ElementInfo;
import common.Metadata;

/**
 *
 * @author urs
 */
public class MetadataReader implements PeekReader<Character> {

  private LinkedList<Metadata> metadata;
  private int pos;
  private Character nextSym = null;

  public MetadataReader(List<Metadata> metadata) {
    this.metadata = new LinkedList<Metadata>(metadata);
    pos = 0;
    next();
  }

  private void closeFile() {
    metadata.clear();
  }

  public Character peek() {
    return nextSym;
  }

  public boolean hasNext() {
    return peek() != null;
  }

  public Character next() {
    Character sym = nextSym;

    if (metadata.isEmpty()) {
      nextSym = null;
      closeFile();
      return sym;
    } else if (pos >= getActualMeta().getValue().length()) {
      metadata.pop();
      pos = 0;
    }

    if (metadata.isEmpty()) {
      nextSym = null;
      closeFile();
      return sym;
    }

    nextSym = getActualMeta().getValue().charAt(pos);
    pos++;

    return sym;
  }

  public ElementInfo getInfo() {
    ElementInfo metaInfo = getActualMeta().getInfo();
    int keySize = getActualMeta().getKey().length() + 1;
    ElementInfo info = new ElementInfo(metaInfo.getFilename(), metaInfo.getLine(), metaInfo.getRow() + pos + keySize);
    return info;
  }

  private Metadata getActualMeta() {
    return metadata.getFirst();
  }
}
