package org.infinispan.query;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public interface ProjectionConstants {

   String ID = org.hibernate.search.engine.ProjectionConstants.ID;

   /**
    * Just an alias for {@link ProjectionConstants#ID}  //todo [anistor] really??
    */
   String KEY = "__ISPN_Key";

   String VALUE = org.hibernate.search.engine.ProjectionConstants.THIS;
}
