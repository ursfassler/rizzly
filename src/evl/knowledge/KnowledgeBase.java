/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package evl.knowledge;

import java.util.HashMap;

import evl.other.Namespace;

public class KnowledgeBase {
  final private HashMap<Class<? extends KnowledgeEntry>, KnowledgeEntry> entries = new HashMap<Class<? extends KnowledgeEntry>, KnowledgeEntry>();
  final private Namespace root;
  final private String rootdir;

  public KnowledgeBase(Namespace root, String rootdir) {
    super();
    this.root = root;
    this.rootdir = rootdir;
  }

  public String getRootdir() {
    return rootdir;
  }

  public Namespace getRoot() {
    return root;
  }

  public void clear() {
    entries.clear();
  }

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
