package com.razie.pubstage.data;

import java.util.ArrayList;
import java.util.List;


/**
 * a list structure
 * 
 * TODO detailed docs
 * 
 * @author razvanc
 * @param [T]
 */
trait ListStruc[T] extends Structure[List[T]] {
}

class ListStrucImpl[T] (t:List[T]) extends StrucImpl[List[T]] (t) with ListStruc[T] {
}
