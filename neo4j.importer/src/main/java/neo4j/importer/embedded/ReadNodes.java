package neo4j.importer.embedded;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

/***
 * Read nodes from a file, put them under a specific label and create index for a specific property.
 * Give 1. file path, 2. label name and 3. index name.
 * @author roxanne
 *
 */
public class ReadNodes extends ProcessWrapper {
	
	private static String filePath;
	private static String labelName;
	private static String indexName;
	
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Please give args [1] [2] [3].");
			System.out
					.println("[1] is file path, [2] is label name, [3] is index name(should from the header key).");
			return;
		}
		filePath = args[0];
		labelName = args[1];
		indexName = args[2];
		
		ReadNodes readNodes = new ReadNodes();
		readNodes.runProcess();
	}

	@Override
	protected void transactionProcess() {
		IndexDefinition indexDefinition;
		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			indexDefinition = schema.indexFor(DynamicLabel.label(labelName))
					.on(indexName).create();
			tx.success();
		}
		try (Transaction tx = graphDb.beginTx()) {
			Schema schema = graphDb.schema();
			schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
		}

		try (Transaction tx = graphDb.beginTx()) {
			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read File Line By Line
			String[] headers = null;
			while ((strLine = br.readLine()) != null) {
				String[] columns = strLine.split(",");
				if (headers == null) {
					headers = columns;
				} else {
					createNode(graphDb, headers, columns, labelName);
				}
			}
			// Close the input stream
			tx.success();
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		} finally {
			graphDb.shutdown();
		}

	}
	
	private Node createNode(final GraphDatabaseService graphDb,
			final String[] headers, final String[] columns, final String label) {
		// Create Node and add Label
		Node node = graphDb.createNode();
		for (int i = 0; i < columns.length; i++) {
			node.setProperty(headers[i], columns[i]);
		}
		node.addLabel(DynamicLabel.label(label));
		return node;
	}

}
