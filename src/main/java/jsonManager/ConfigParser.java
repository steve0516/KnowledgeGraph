package jsonManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jsonManagerModels.GraphEntity;

/*
 * This class reads a config file and creates the map we need to manipulate.
 * This class saves the relations in a map. Each map contains as value the map representing
 * the entity connected with that relation.
 * 
 * In the config file:
 * " " separates elements;
 * ":" separates the fields for each element
 * "." separates the gerarchy in the json file
 * "-" separates different attributes
 */

public class ConfigParser {

	public Map<String, LinkedHashMap<GraphEntity, ArrayList<GraphEntity>>> relations;

	private static final String confPath="/home/bum-bum/Desktop/jsonConfig.txt";
	private static final String jsonPath="/home/bum-bum/Desktop/demo.txt";

	public ConfigParser(){
		this.relations = new LinkedHashMap<String, LinkedHashMap<GraphEntity, ArrayList<GraphEntity>> >();
	}

	private void readFields() throws IOException, ParseException{
		FileReader input = new FileReader(confPath);
		BufferedReader bufRead = new BufferedReader(input);
		String myLine = null;

		while ( (myLine = bufRead.readLine()) != null)
		{    
			//Array has 3 elements: REL, KEY, VALUE
			String[] array = myLine.split(" ");
			//Rel is the key for the external map
			String rel = array[0];
			//This key and value are for the inner map
			String key = array[1];
			String value = array[2];

			//This list has in pos 0 the json field that represents the key (could be nested); the other field are the attributes
			ArrayList<String> keyToExtract = new ArrayList<String>();
			keyToExtract.add(key.split(",")[0].split(":")[1]);
			for (int i=0;i<key.split(",")[1].split(":")[1].split("-").length; i++)
				keyToExtract.add(key.split(",")[1].split(":")[1].split("-")[i]);
			//Now the first list to extract keys is ready

			//Same type list but for values
			ArrayList<String> valueToExtract = new ArrayList<String>();
			valueToExtract.add(value.split(",")[0].split(":")[1]);
			for (int i=0;i<value.split(",")[1].split(":")[1].split("-").length; i++)
				valueToExtract.add(value.split(",")[1].split(":")[1].split("-")[i]);
			/************************************** fin qui ok: l'output è
			 * [user, NoNo]
				[interest.all, category, display, score]
			System.out.println(keyToExtract);
			System.out.println(valueToExtract);*/	
			json2map(keyToExtract, valueToExtract);
		}
	}

	private void insertRel(LinkedHashMap<String, LinkedHashMap<GraphEntity, ArrayList<GraphEntity>>> map, String rel){
		String relation = rel.split(":")[1];
		System.out.println(relation);
	}

	//Extracts nested values form the json
	public GraphEntity keyExtraction(JSONObject obj, ArrayList<String> key){
		//For on nested values of key.
		String id="";
		String attributes="";
		String[] nest = null;
		JSONParser parser = new JSONParser();
		JSONObject tmp=obj;
		
		if (key.get(0).split("\\.").length>1)
			nest = key.get(0).split("\\.");
		else nest = new String[] {key.get(0)};
		for (int i=0; i<nest.length ;i++){
			if (nest.length-i==1){
				id= tmp.get(nest[i]).toString();
				for (String attr : key.subList(1, key.size())){
					attributes+=attr+":";
					attributes+=String.valueOf(tmp.get(attr));
				}
			}
			else {
				try {
					System.out.println(tmp.get(nest[i]));
					tmp = (JSONObject) parser.parse((String) tmp.get(nest[i]));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//tmp =  (JSONObject) tmp.get(nest[i]);
				//System.out.println(tmp.get(nest[i]));
			}
		}
		//System.out.println(tmp);
		return new GraphEntity(id, attributes);
	}
	//Difference from keyExtraction is that key is single value, while associated values are a list of Entity 
	public ArrayList<GraphEntity> valuesExtraction(JSONObject obj, ArrayList<String> key){
		ArrayList<GraphEntity> linkedEntities = new ArrayList<GraphEntity>();
		JSONParser parser = new JSONParser();
		
		return linkedEntities;
	}
	
	
	
	//This method reads the JSON and extracts the field for each relation
	private LinkedHashMap<GraphEntity, ArrayList<GraphEntity>> json2map( ArrayList<String> key, ArrayList<String> value) throws FileNotFoundException, ParseException{
		LinkedHashMap<GraphEntity, ArrayList<GraphEntity>> jsonMap=new LinkedHashMap<GraphEntity, ArrayList<GraphEntity>>();
		JSONParser parser = new JSONParser();
		//This part because the input is in txt
		Scanner scanner = new Scanner(new File(jsonPath));
		scanner.useDelimiter("\n");
		while (scanner.hasNext()) {
			String linea = scanner.next();
			linea = linea+""+scanner.next();
			scanner.next();
			linea = "{"+linea+"}";
			//System.out.println(linea);
			if(!linea.equals("{\n}")){
				JSONObject json = (JSONObject) parser.parse(linea);

				/*System.out.println("La lista è:");
				System.out.println(key.get(0));
				printAr(key.get(0).split("."));*/

				GraphEntity keyEntity = keyExtraction(json, key);
				System.out.println(keyEntity.getId() +" "+keyEntity.getAttr());

				ArrayList<GraphEntity> valuesEntity = valuesExtraction(json, value);
				//System.out.println(valueEntity.getId() +" "+valueEntity.getAttr());
				jsonMap.put(keyEntity, valuesEntity);
				JSONObject info = (JSONObject)json.get("info");

				JSONObject interests = (JSONObject)info.get("interests");
				JSONObject all = (JSONObject)interests.get("all");
				List<String> interestIDS = new ArrayList<String>(all.keySet());

				TreeMap<String, String> interestID_info = new TreeMap<String, String>();
			}
		}



		return jsonMap;
	}

	private void printAr(String[] ar){
		for (int i=0; i<ar.length; i++)
			System.out.println(ar[i]+" ");

	}

	public static void main(String[] args) throws ParseException{
		ConfigParser p = new ConfigParser();
		try {
			p.readFields();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
