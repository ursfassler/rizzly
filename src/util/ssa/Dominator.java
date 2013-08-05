/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package util.ssa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;

//DirectedGraph<BasicBlock, BbEdge>

/**
 * Computes the intermediate dominators of the nodes in graph g.
 * Uses the algorithm from the paper "A Simple , Fast Dominance Algorithm" by Cooper, Keith D and Harvey, Timothy J and Kennedy, Ken
 *
 * @author urs
 *
 * @param <V>
 * @param <E>
 */
public class Dominator<V,E> implements DirectedGraph<V, Edge<V>> {
	private HashMap<V,V>		dom = new HashMap<V,V>();
	private HashMap<V,Integer>	lvl = new HashMap<V,Integer>();
	private DirectedGraph<V,E> 	g;

	public Dominator(DirectedGraph<V, E> g) {
		super();
		this.g = g;
	}

	public HashMap<V, V> getDom() {
		return dom;
	}

	public V getEntry( Set<V> vertices ){
		V res = null;
		for( V v : vertices ){
			if( g.inDegreeOf(v) == 0 ){
				if( res != null ){
					new RuntimeException( "Only graphs with one entry point allowed" );
				}
				res = v;
			}
		}
		if( res == null ){
			new RuntimeException( "Entry point of graph not found" );
		}
		return res;
	}

	public List<V> reversePostorder(){
		Set<V>			vertices	= new HashSet<V>( g.vertexSet() );
		LinkedList<V> 	res = new LinkedList<V>();

		int s = vertices.size();

		revpost( getEntry(vertices), res, vertices );

		assert( vertices.isEmpty() );
		assert( res.size() == s );

		return res;
	}


	private void revpost(V v, LinkedList<V> res, Set<V> vertices) {
		if( !vertices.contains(v) ){
			return;
		}
		vertices.remove(v);
		for( E e : g.outgoingEdgesOf(v) ){
			revpost( g.getEdgeTarget(e), res, vertices );
		}
		res.push(v);
	}

	public void calc(){
		for( V v : g.vertexSet() ){
			dom.put(v, null);
			lvl.put(v, -1);
		}
		V start = getEntry(g.vertexSet());
		dom.put(start, start);
		lvl.put(start,0);

		List<V> revPost = reversePostorder();

		boolean changed = true;
		while( changed ){
			changed = false;
			for( V b : revPost ){
				if( b == start ){
					continue;
				}
				Set<V> ppl = procPred( b );
				V new_idom = ppl.iterator().next();
				for( V p : ppl ){
					if( dom.get(p) != null ){
						new_idom = intersect( p, new_idom );
					}
				}
				if( dom.get(b) != new_idom ){
					dom.put(b, new_idom);
					lvl.put(b, lvl.get(new_idom)+1);
					changed	= true;
				}
			}
		}
		dom.put(start, null);
	}

	private V intersect(V a, V b) {
		assert( lvl.get(a) >= 0 );
		assert( lvl.get(b) >= 0 );
		while( a != b ){
			assert( lvl.get(a) >= 0 );
			assert( lvl.get(b) >= 0 );
			while( (lvl.get(a) >= lvl.get(b)) && (a != b) ){
				a = dom.get(a);
				assert( lvl.get(a) >= 0 );
			}
			while( (lvl.get(b) >= lvl.get(a)) && (a != b) ){
				b = dom.get(b);
				assert( lvl.get(b) >= 0 );
			}
		}
		return a;
	}

	/**
	 * Returns the set of already processed predecessors of b
	 * @param g
	 * @param dom
	 * @param b
	 * @return
	 */
	private Set<V> procPred( V b) {
		Set<V> 	res = new HashSet<V>();
		for( E e : g.incomingEdgesOf(b) ){
			V u = g.getEdgeSource(e);
			if( dom.get( u ) != null ){
				res.add(u);
			}
		}
		return res;
	}


	public Edge<V> addEdge(V arg0, V arg1) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public boolean addEdge(V arg0, V arg1, Edge<V> arg2) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public boolean addVertex(V arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public boolean containsEdge(Edge<V> arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public boolean containsEdge(V arg0, V arg1) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public boolean containsVertex(V arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public Set<Edge<V>> edgeSet() {
		Set<Edge<V>>	res = new HashSet<Edge<V>>();

		for( V v : dom.keySet() ){
			res.add( new SimpleEdge<V>( dom.get(v), v ) );
		}

		return res;
	}


	public Set<Edge<V>> edgesOf(V arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public Set<Edge<V>> getAllEdges(V arg0, V arg1) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public Edge<V> getEdge(V arg0, V arg1) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public EdgeFactory<V, Edge<V>> getEdgeFactory() {
		throw new RuntimeException( "Not yet implemented" );
	}


	public V getEdgeSource(Edge<V> arg0) {
		return arg0.getSrc();
	}


	public V getEdgeTarget(Edge<V> arg0) {
		return arg0.getDst();
	}


	public double getEdgeWeight(Edge<V> arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public boolean removeAllEdges(Collection<? extends Edge<V>> arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public Set<Edge<V>> removeAllEdges(V arg0, V arg1) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public boolean removeAllVertices(Collection<? extends V> arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public boolean removeEdge(Edge<V> arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public Edge<V> removeEdge(V arg0, V arg1) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public boolean removeVertex(V arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public Set<V> vertexSet() {
		return dom.keySet();
	}


	public int inDegreeOf(V arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public Set<Edge<V>> incomingEdgesOf(V arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public int outDegreeOf(V arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}


	public Set<Edge<V>> outgoingEdgesOf(V arg0) {
		throw new RuntimeException( "Not yet implemented" );
	}
}
