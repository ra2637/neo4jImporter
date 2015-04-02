package neo4j.importer.embedded;

import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;

/***
 * Generate legacy index for nodes.
 * Input: [1] node label, [2] property key name to be indexed.
 * @author roxanne
 *
 */
public class CreateLegacyIndex extends ProcessWrapper {
	private static String nodeLabel;
	private static String propertyName;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Please give args [1] [2].");
			System.out
					.println("[1] is node label, [2] is property name to be indexed.");
			return;
		}

		nodeLabel = args[0];
		propertyName = args[1];
		CreateLegacyIndex createLegacyIndex = new CreateLegacyIndex();
		createLegacyIndex.runProcess();
	}
	
	@Override
	protected void transactionProcess() {
		Label label = DynamicLabel.label(nodeLabel);
		Node node = null;
		GlobalGraphOperations graphOperations = GlobalGraphOperations
				.at(graphDb);
		Index<Node> nodeIndex;
		
		try(Transaction tx = graphDb.beginTx()) {
			IndexManager index = graphDb.index();
			if(index.existsForNodes(propertyName)){
				nodeIndex = index.forNodes(propertyName);
				nodeIndex.delete();
				System.out.println("Index "+propertyName+" exists, deleting...");
			}
			tx.success();
			tx.close();
		}
		
		try(Transaction tx = graphDb.beginTx()) {
			System.out.println("Creating index: "+propertyName+"...");
			IndexManager index = graphDb.index();
			nodeIndex = index.forNodes(propertyName);
			try(ResourceIterator<Node> nodes = graphOperations
					.getAllNodesWithLabel(label)
					.iterator()) {
				while (nodes.hasNext()) {
					node = nodes.next();
					nodeIndex.add(node, propertyName, node.getProperty(propertyName));
				}
				nodes.close();
			}
			tx.success();
			tx.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		} finally {
			graphDb.shutdown();
		}
	}

}
