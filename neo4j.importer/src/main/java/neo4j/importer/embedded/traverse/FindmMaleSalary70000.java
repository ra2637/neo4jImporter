package neo4j.importer.embedded.traverse;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

import neo4j.importer.embedded.ProcessWrapper;
/***
 * Find male employees whose salary now is 70000. Search by schema index.
 * Cypher:
 * MATCH (s:SALARY {salary:"70000"})<-[hs:has_salary {to_date: "9999-01-01"}]-(e:EMPLOYEE {gender:"M"})
 * return count(e);
 * @author roxanne
 *
 */
public class FindmMaleSalary70000 extends ProcessWrapper {
	public enum RelTypes implements RelationshipType {
		has_salary
	}

	public enum Labels implements Label {
		SALARY
	}

	public static void main(String[] args) {
		FindmMaleSalary70000 findMaleSalary70000 = new FindmMaleSalary70000();
		findMaleSalary70000.runProcess();
	}

	@Override
	protected void transactionProcess() {
		try (Transaction tx = graphDb.beginTx()) {
			try(ResourceIterator<Node> salary70000Node = graphDb.findNodesByLabelAndProperty(Labels.SALARY, "salary", "70000").iterator()){
				int count = 0;
				while(salary70000Node.hasNext()){
					Node s = salary70000Node.next();				
					Traverser employeeTraverser = getEmployees(s);
					for(Path employeePath: employeeTraverser){
						Iterator<Relationship> rels = employeePath.relationships().iterator();
						Relationship rel;
						while(rels.hasNext()){
							rel = rels.next();
							if(rel.getProperty("to_date").equals("9999-01-01")){
								if(employeePath.endNode().getProperty("gender").equals("M")){
									count ++;
								}
							}
						}
					}
				}
				System.out.println("count: "+count);
				salary70000Node.close();
			}
			
			tx.success();
			tx.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		} finally {
			graphDb.shutdown();
		}
	}

	private Traverser getEmployees(final Node salary) {
		TraversalDescription td = graphDb.traversalDescription().breadthFirst()
				.relationships(RelTypes.has_salary, Direction.INCOMING)
				.evaluator(Evaluators.all());
		return td.traverse(salary);
	}

}
