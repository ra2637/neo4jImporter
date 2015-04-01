# neo4j Importer
Basically, the project is customized for the employee_graph data. But you can modify for your own use.
  
This project goal is to load data to neo4j by JAVA API.
The DB is from MySQL [Employees Sample Database](https://dev.mysql.com/doc/employee/en/employees-introduction.html).
  
### How to use
- Prepare maven.
- Modify the EMBEDDED_DB_URI string in neo4j.importer.config class to indicate the DB storage path.
- Complie the code.
- Run it.
```sh
  $ mvn dependency:copy-dependencies
  $ java -cp target/neo4j.importer-0.0.1-SNAPSHOT.jar:target/dependency/* neo4j.importer.embedded.ReadNodes param1 param2
```
- Note must run ReadNodes first then ReadRelations.
