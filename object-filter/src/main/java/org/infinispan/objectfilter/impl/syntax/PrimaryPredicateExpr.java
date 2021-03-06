package org.infinispan.objectfilter.impl.syntax;

/**
 * @author anistor@redhat.com
 * @since 7.0
 */
public interface PrimaryPredicateExpr extends BooleanExpr {

   /**
    * Returns the left child value expression to which the predicate is attached. The {@link ValueExpr} should always be
    * a {@link PropertyValueExpr} after the tree was normalized and constant expressions were removed.
    */
   ValueExpr getChild();
}
