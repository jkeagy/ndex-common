package org.ndexbio.common.access;

import java.util.logging.Logger;

import org.ndexbio.common.exceptions.NdexException;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class NdexAOrientDBConnectionPool {
	
	private static NdexAOrientDBConnectionPool INSTANCE = null;

//	private static int poolSize = 50;
//	private static final String dbURLPropName  = "OrientDB-URL";
//	private static final String dbUserPropName = "OrientDB-Username";
	
	private ODatabaseDocumentPool pool;
	
	
	private static final Logger logger = Logger
			.getLogger(NdexAOrientDBConnectionPool.class.getName());

	private NdexAOrientDBConnectionPool(String dbURL, String dbUserName, String dbPassword, int size) {

		OGlobalConfiguration.CACHE_LEVEL1_ENABLED.setValue(true);
		OGlobalConfiguration.CACHE_LEVEL2_ENABLED.setValue(false);
		OGlobalConfiguration.STORAGE_KEEP_OPEN.setValue( true );
		OGlobalConfiguration.STORAGE_COMPRESSION_METHOD.setValue( "nothing" );
		OGlobalConfiguration.STORAGE_USE_CRC32_FOR_EACH_RECORD.setValue(true);
		
		
		pool = new ODatabaseDocumentPool(dbURL, dbUserName, dbPassword);
	    pool.setup(1,size);
	    
	    //TODO: check if we need to set a timeout value for it. Most likely not.
	    //OGlobalConfiguration.CLIENT_CONNECT_POOL_WAIT_TIMEOUT.setValue("3600000");
	    
	    logger.info("Connection pool to " + dbUserName + "@" + dbURL + " created.");
	}
	
	public static synchronized void createOrientDBConnectionPool (String dbURL, String dbUserName,
				String dbPassword, int size) {
	      if(INSTANCE == null) {
		         INSTANCE = new NdexAOrientDBConnectionPool(dbURL, dbUserName, dbPassword, size);
	      }
	}
	
	public static synchronized NdexAOrientDBConnectionPool getInstance() throws NdexException {
	      if(INSTANCE == null) {
	         throw new NdexException ("Connection pool is not created yet.");
	      }
	      return INSTANCE;
	}

	public ODatabaseDocumentTx acquire() {
		ODatabaseDocumentTx conn = pool.acquire();
		
	    return conn;
	}
   	
   public static synchronized void close() { 	
	 
       if ( INSTANCE != null) {	   
	     INSTANCE.pool.close();
	     INSTANCE=null;
       }
       logger.info("Connection pool closed.");
   }
   
   
}
