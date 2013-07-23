/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package fun.knowledge;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import fun.other.Namespace;
import fun.other.RizzlyFile;

public class KnowledgeBase {
  final private HashMap<Class<? extends KnowledgeEntry>, KnowledgeEntry> entries = new HashMap<Class<? extends KnowledgeEntry>, KnowledgeEntry>();
  final private Namespace root;
  final private Set<RizzlyFile> files;
  final private String rootdir;

  public KnowledgeBase(Namespace root, Collection<RizzlyFile> files, String rootdir) {
    super();
    this.root = root;
    this.files = new HashSet<RizzlyFile>( files );
    this.rootdir = rootdir;
  }

  public String getRootdir() {
    return rootdir;
  }

  public Namespace getRoot() {
    return root;
  }

  public Set<RizzlyFile> getFiles() {
    return files;
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
