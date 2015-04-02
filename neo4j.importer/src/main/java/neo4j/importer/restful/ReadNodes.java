package neo4j.importer.restful;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import neo4j.importer.config.Server;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/***
 * Read nodes from a file, put them under a specific label and create index for a specific property.
 * Give 1. file path, 2. label name.
 * @author roxanne
 *
 */
public class ReadNodes {
	static WebResource resource = Client.create().resource(Server.SERVER_NODE_URI);
	static Date startDate = null;
	static Date endDate = null;
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Please give args [1] [2].");
			System.out.println("[1] is file path, [2] is label name.");
			return;
		}
		
		String filePath = args[0];
		String labelName = args[1];

		try {
			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			startDate = new Date();
			String[] headers = null;
			while ((strLine = br.readLine()) != null) {
				String[] columns = strLine.split(",");
				if(headers == null){
					headers = columns;
				}else{
					JSONObject propJson = new JSONObject();
					for(int i=0; i<columns.length; i++){
						propJson.put(headers[i], columns[i]);
					}
					JSONObject node = createNode(propJson);
					addLabel(node.getString("labels"), labelName);
				}
			}
			// Close the input stream
			endDate = new Date();
			in.close();
			System.out.println("Execute time: " + (endDate.getTime() - startDate.getTime()) + " ms.");
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	static private JSONObject createNode(JSONObject properties){
		ClientResponse response = resource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.entity(properties.toString())
				.post(ClientResponse.class);
		
		JSONObject output = null;
		if(response.getStatus() != 201){
			// error
			System.out.println(response.getEntity(String.class));
		}else{
			output = (JSONObject) JSONSerializer.toJSON(response.getEntity(String.class));
		}
		return output;
	}
	
	static private JSONObject addLabel(String nodeUri, String label){
		WebResource node = Client.create().resource(nodeUri);
		ClientResponse response = node.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.entity("\""+label+"\"")
				.post(ClientResponse.class);
		
		JSONObject output = null;
		if(response.getStatus() == 400){
			// error
			System.out.println(response.getEntity(String.class));
		}else{
			// Sccess return null.
		}
		return output;
	}
}