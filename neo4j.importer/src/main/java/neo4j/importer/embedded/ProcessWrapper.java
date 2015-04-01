package neo4j.importer.embedded;

import java.util.Date;
import neo4j.importer.config.Server;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/***
 * Handle neo4j embedded server shutdown and calculate the time the process cost.
 * @author roxanne
 */
public abstract class ProcessWrapper {
	protected static GraphDatabaseService graphDb;
	protected static ExecutionEngine engine = null;
	private static Date startDate = null;
	private static Date endDate = null;
	protected void runProcess(){
		startDate = new Date();
		graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(Server.EMBEDDED_DB_URI);
		engine = new ExecutionEngine(graphDb);
		registerShutdownHook(graphDb);
		transactionProcess();
	}
	
	protected abstract void transactionProcess(); 
	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
				System.out.println("Shutdown");
				endDate = new Date();
				System.out.println("Execute time: "
						+ (endDate.getTime() - startDate.getTime()) + " ms.");
			}
		});
	}
}
