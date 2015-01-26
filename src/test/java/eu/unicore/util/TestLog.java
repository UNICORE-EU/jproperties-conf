package eu.unicore.util;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import junit.framework.TestCase;

public class TestLog extends TestCase{

	public void testExceptionLogging(){
		Logger logger = Log.getLogger("test", TestLog.class);
		logger.setLevel(Level.INFO);
		boolean logged = Log.logException("Test error", new IOException(), logger);
		assertTrue(logged);
		for(int i = 0; i<100; i++){
			logged = Log.logException("Test error", new IOException(), logger);
			assertFalse(logged);
		}
		// should have single entry in TS map
		assertEquals(1, Log.errorLogTimes.size());
		// expire it...
		Integer key = Log.errorLogTimes.keySet().iterator().next();
		Log.errorLogTimes.put(key, System.currentTimeMillis()-61000);
		// and we should log again
		logged = Log.logException("Test error", new IOException(), logger);
		assertTrue(logged);
		// check expiry of entries
		logger.setLevel(Level.OFF); // do not flood console out
		for(int i = 0; i<1000; i++){
			logged = Log.logException("Test error "+i, new IOException(), logger);
			assertTrue(logged);
		}
		assertTrue(Log.errorLogTimes.size()<=500);
		Iterator<Integer>keys = Log.errorLogTimes.keySet().iterator();
		while(keys.hasNext()){
			Log.errorLogTimes.put(keys.next(), System.currentTimeMillis()-61000);
		}
		logged = Log.logException("Test error", new IOException(), logger);
		assertTrue(logged);
		assertEquals(1, Log.errorLogTimes.size());
	}
	
}
