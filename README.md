# neo4j Importer
Basically, the project is customized for the [employee_graph data](https://github.com/ra2637/neo4jImporter/tree/master/employees_graph). But you can modify for your own use.
  
This project goal is to load data to neo4j by JAVA API and restful API.
The DB is from MySQL [Employees Sample Database](https://dev.mysql.com/doc/employee/en/employees-introduction.html).
  
### Implement backgroup
- Create nodes first then find 2 nodes and generate relationship.
- JAVA API: 
    - Implement with neo4j JAVA API.
    - Support Schema and legacy index.
    - Auto create schema when create nodes.
    - Should stop the neo4j server when using.
- RESTful API:
    - impelement with neo4j RESTful API.
    - Not support schema and legacy index.
    - ReadRelations using cypher query to find the nodes and generate the relationship between the nodes.

### How to use 
- Prepare maven.
- JAVA API: neo4j.importer.embedded 
    - Modify the EMBEDDED_DB_URI string in neo4j.importer.config class to indicate the DB storage path.
- RESTful API: neo4j.importer.restful
    - Modify the SERVER_ROOT_URI string in neo4j.importer.config class to indicate the DB storage path.
- Complie the code.
- Run it. 
- Note must run ReadNodes first then ReadRelations.
- Examples:
    - JAVA API:
```sh
  $ mvn dependency:copy-dependencies
  $ java -cp target/neo4j.importer-0.0.1-SNAPSHOT.jar:target/dependency/* neo4j.importer.embedded.ReadNodes ../employees_graph/departments.csv DEPARTMENT dept_no 
```
    - RESTful API:
```sh
  $ mvn dependency:copy-dependencies
  $ java -cp target/neo4j.importer-0.0.1-SNAPSHOT.jar:target/dependency/* neo4j.importer.restful.ReadNodes ../employees_graph/departments.csv DEPARTMENT
```

### Functions
- JAVA API:
    - Create nodes: neo4j.importer.embedded.ReadNodes [file path] [label name] [index name (from the header key)]
    - Create relationships for nodes: neo4j.importer.embedded.ReadRelations [file path] [fromNodeKey]:[fromNodeLabel] [relationships label name] [toNodeKey]:[toNodeLabel]
    - Create legacy index: neo4j.importer.embedded.CreateLegacyIndex [node label] [property key name to be indexed]

- RESTful API:
    - Create nodes: neo4j.importer.restful.ReadNodes [file path] [label name]
    - Create relationships for nodes: neo4j.importer.restful.ReadRelations [file path] [fromNodeKey]:[fromNodeLabel] [relationships label name] [toNodeKey]:[toNodeLabel]

### DB Model and Traverse example.
- Model structure
![Graph Model](https://github.com/ra2637/neo4jImporter/blob/master/images/employee_graph.png?raw=true "Graph Model")

You can use the traverse to see the count result, the travserse is designed under the model.
- Find male employees whose salary now is 70000 (by schema): neo4j.importer.embedded.traverse.FindmMaleSalary70000

### Disadvantage
- Importer make all field become string in neo4j. Thus cannot filter nodes by range. e.g.salary > 7000

### Version
 - neo4j: 2.1.6

