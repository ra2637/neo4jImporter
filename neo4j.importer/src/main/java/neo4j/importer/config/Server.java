package neo4j.importer.config;

public class Server {
	public final static String SERVER_ROOT_URI = "http://localhost:7474/db/data";
	public final static String SERVER_NODE_URI =  SERVER_ROOT_URI+"/node"; 
	public final static String SERVER_LABEL_URI = SERVER_ROOT_URI+"/label";
	public final static String SERVER_BATCH_URI = SERVER_ROOT_URI+"/batch";
	public final static String SERVER_TRANSATIONCOMMIT_URI = SERVER_ROOT_URI+"/transaction/commit";
	public final static String EMBEDDED_DB_URI = "neo4j-community-2.1.6/data/graph.db";
	public final static String EMBEDDED_CONFIG_URI = "neo4j-community-2.1.6/conf/neo4j.properties";
}
