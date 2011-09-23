package com.razie.pubstage.data;

/**
 * this represents a structure (like a tree or a graph) imposed on some contents
 * 
 * @author razvanc
 * @param <T>
 */
trait Structure[T] {
   var contents : T
}

class StrucImpl[T] (override var contents : T) extends Structure[T] {
}
