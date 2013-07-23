/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package cir.knowledge;

import java.util.HashMap;

public class KnowledgeBase {
  final private HashMap<Class<? extends KnowledgeEntry>, KnowledgeEntry> entries = new HashMap<Class<? extends KnowledgeEntry>, KnowledgeEntry>();
  final private String rootdir;

  public KnowledgeBase(String rootdir) {
    super();
    this.rootdir = rootdir;
  }

  public String getRootdir() {
    return rootdir;
  }

  public void clear() {
    entries.clear();
  }

  @SuppressWarnings("unchecked")
  public <T extends KnowledgeEntry> T getEntry(Class<T> id) {
    if (!entries.containsKey(id)) {
      KnowledgeEntry entry;
      try {
        entry = id.newInstance();
        entry.init(this);
        entries.put(id, entry);
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return (T) entries.get(id);
  }
}
