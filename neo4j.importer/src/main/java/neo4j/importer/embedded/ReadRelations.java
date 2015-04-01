package neo4j.importer.embedded;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

/***
 * Read relationships between 2 nodes. The nodes must already exist in the DB.
 * Read 1000 lines and send as a transaction.
 * Main input: [1] file path, [2] fromNodeKey:fromNodeLabel, [3] relationships label name, [4] toNodeKey:toNodeLabel.
 * @author roxanne
 *
 */
public class ReadRelations extends ProcessWrapper {
	private static String filePath;
	private static String fromNodeKey;
	private static String fromNodeLabel;
	private static String labelName;
	private static String toNodeKey;
	private static String toNodeLabel;
	
	private int count=0, size=1000;
	private ArrayList<Node> fromNodes = new ArrayList<Node>();
	private ArrayList<Node> toNodes = new ArrayList<Node>();
	private ArrayList<String[]> relProps = new ArrayList<String[]>();
	
	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Please give args [1] [2] [3] [4].");
			System.out
					.println("[1] is file path, [2] is from node key:label, [3] is rel label name, [4] is to node key:label.");
			return;
		}

		filePath = args[0];
		fromNodeKey = args[1].split(":")[0];
		fromNodeLabel = args[1].split(":")[1];
		labelName = args[2];
		toNodeKey = args[3].split(":")[0];
		toNodeLabel = args[3].split(":")[1];

		ReadRelations readRelations = new ReadRelations();
		readRelations.runProcess();
	}

	@Override
	protected void transactionProcess() {
		try {
			// Read File Line By Line
			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String[] headers = null;
			while ((strLine = br.readLine()) != null) {
				String[] columns = strLine.split(",");
				if (headers == null) {
					headers = columns;
				} else {
					storeNodesAndRelationship(headers, columns);
					count++;
					if (count >= size) {
						try (Transaction tx = graphDb.beginTx()) {
							createRelationships(headers);
							tx.success();
							tx.close();
							count -= size;
							fromNodes = new ArrayList<Node>();
							toNodes = new ArrayList<Node>();
							relProps = new ArrayList<String[]>();
						}
					}
				}
			}
			if (count != 0) {
				try (Transaction tx = graphDb.beginTx()) {
					createRelationships(headers);
					tx.success();
					tx.close();
				}
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		} finally {
			graphDb.shutdown();
		}
	}
	
	
	private void storeNodesAndRelationship(final String[] headers,
			final String[] columns) {
		Node fromNode = null, toNode = null;
		for (int i = 0; i < columns.length; i++) {
			if (headers[i].equals(fromNodeKey)) {
				fromNode = findNodeByProperty(fromNodeLabel, fromNodeKey,
						columns[i]);
				fromNodes.add(fromNode);
			} else if (headers[i].equals(toNodeKey)) {
				toNode = findNodeByProperty(toNodeLabel, toNodeKey, columns[i]);
				toNodes.add(toNode);
			}
			if (fromNode != null && toNode != null) {
				break;
			}
		}
		relProps.add(columns);
	}
	private void createRelationships(final String[] headers) {
		Node fromNode = null, toNode = null;
		Relationship rel = null;
		String[] relProp = null;
		for (int i = 0; i < fromNodes.size(); i++) {
			fromNode = fromNodes.get(i);
			toNode = toNodes.get(i);
			relProp = relProps.get(i);
			rel = fromNode.createRelationshipTo(toNode,
					DynamicRelationshipType.withName(labelName));
			for(int j=0; j<relProp.length; j++){
				if (!headers[j].equals(fromNodeKey)
						&& !headers[j].equals(toNodeKey)) {
					rel.setProperty(headers[j], relProp[j]);
				}
			}
		}
	}
	
	private Node findNodeByProperty(final String labelName,
			final String indexName, final String value) {
		Label label = DynamicLabel.label(labelName);
		Node node = null;
		try (Transaction tx = graphDb.beginTx()) {
			try (ResourceIterator<Node> nodes = graphDb
					.findNodesByLabelAndProperty(label, indexName, value)
					.iterator()) {
				if (nodes.hasNext()) {
					node = nodes.next();
				}
				nodes.close();
			}
			tx.success();
			tx.close();
			return node;
		}
	}
}
