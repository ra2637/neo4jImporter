package neo4j.importer.restful;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import neo4j.importer.config.Server;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/***
 * Read relationships between 2 nodes. The nodes must already exist in the DB.
 * Read 1000 lines and send as a transaction.
 * Main input: [1] file path, [2] fromNodeKey:fromNodeLabel, [3] relationships label name, [4] toNodeKey:toNodeLabel.
 * @author roxanne
 *
 */
public class ReadRelations {
	
	static Date startDate = null;
	static Date endDate = null;
	static String filePath;
	static String fromNodeKey;
	static String fromNodeLabel;
	static String labelName;
	static String toNodeKey;
	static String toNodeLabel;
	
	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Please give args [1] [2] [3] [4].");
			System.out.println("[1] is file path, [2] is from node key:label, [3] is rel label name, [4] is to node key:label.");
			return;
		}
		
		filePath = args[0];
		fromNodeKey = args[1].split(":")[0];
		fromNodeLabel = args[1].split(":")[1];
		labelName = args[2];
		toNodeKey = args[3].split(":")[0];
		toNodeLabel = args[3].split(":")[1];
		
		try {
			startDate = new Date();
			// Read File Line By Line
			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String[] headers = null;
			int count = 0;
			int sendSize = 1000;
			
			JSONArray requestArr = new JSONArray();
			while ((strLine = br.readLine()) != null) {
				String[] columns = strLine.split(",");
				if(headers == null){
					headers = columns;
				}else{
					count++;
					requestArr.add(getCypherQuery(columns, headers));
					if(count == sendSize){
						sendRequest(requestArr);
						count -= sendSize;
						requestArr = new JSONArray();
					}
				}
			}
			if(count != 0){
				System.out.println("end program;");
				sendRequest(requestArr);
			}
			// Close the input stream
			endDate = new Date();
			in.close();
			System.out.println("Execute time: " + (endDate.getTime() - startDate.getTime()) + " ms.");
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		} finally{
			
		}
	}

	static private String sendRequest(JSONArray requestArr){
		JSONObject statements = new JSONObject();
		statements.put("statements", requestArr);
		WebResource resource = Client.create().resource(Server.SERVER_TRANSATIONCOMMIT_URI);
		ClientResponse response = resource.type(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.entity(statements.toString())
				.post(ClientResponse.class);
		return response.getEntity(String.class);
	}
	static private JSONObject getCypherQuery(String[] columns, String[] headers){
		JSONObject jsonObject = new JSONObject();
		HashMap from = new HashMap<String, String>();
		HashMap to = new HashMap<String, String>();
		HashMap relationsProps = new HashMap<String, String>();
		for(int i=0; i<columns.length; i++){
			if(headers[i].equals(fromNodeKey)){
				from.put(fromNodeKey, columns[i]);
			}else if(headers[i].equals(toNodeKey)){
				to.put(headers[i], columns[i]);
			}else{
				relationsProps.put(headers[i], columns[i]);
			}
		}
		String cypherQuery = "MATCH (from:"+fromNodeLabel+" "+getPropertyForCypher(from)
				+"), (to:"+toNodeLabel+" "+getPropertyForCypher(to)+") "
				+"CREATE UNIQUE(from)-[:"+labelName+" "+getPropertyForCypher(relationsProps)
				+"]->(to)";
		jsonObject.put("statement", cypherQuery);
		return jsonObject;
	}
	static private String getPropertyForCypher(Map map){
		String str = null;
		Iterator it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        if(str == null){
	        	str = "{";
	        }else{
	        	str += ",";
	        }
	        str+=pairs.getKey()+":\""+pairs.getValue()+"\"";
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	    str+="}";
	    
		return str;
	}
}

