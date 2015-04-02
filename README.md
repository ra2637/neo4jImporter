# neo4j Importer
Basically, the project is customized for the [employee_graph data](https://github.com/ra2637/neo4jImporter/tree/master/employees_graph). But you can modify for your own use.
  
This project goal is to load data to neo4j by JAVA API.
The DB is from MySQL [Employees Sample Database](https://dev.mysql.com/doc/employee/en/employees-introduction.html).
  
### How to use
- Prepare maven.
- Modify the EMBEDDED_DB_URI string in neo4j.importer.config class to indicate the DB storage path.
- Complie the code.
- Run it.
```sh
  $ mvn dependency:copy-dependencies
  $ java -cp target/neo4j.importer-0.0.1-SNAPSHOT.jar:target/dependency/* neo4j.importer.embedded.ReadNodes ../employees_graph/departments.csv DEPARTMENT dept_no
```
- Note must run ReadNodes first then ReadRelations.

### Functions
- Create nodes: neo4j.importer.embedded.ReadNodes [file path] [label name] [index name (from the header key)]
- Create relationships for nodes: neo4j.importer.embedded.ReadRelations [file path] [fromNodeKey]:[fromNodeLabel] [relationships label name][toNodeKey]:[toNodeLabel]
- Create legacy index: neo4j.importer.embedded.CreateLegacyIndex [node label] [property key name to be indexed]

### Version
 - neo4j: 2.1.6
