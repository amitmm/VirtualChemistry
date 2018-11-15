package com.ibm.virtualchemistry;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

//Class to hold information for reaction options for teacher
public class JSONValues {
	public String chemical1Name;
	public String chemical1Formula;
	public String chemical1Color;
	
	public String chemical2Name;
	public String chemical2Formula;
	public String chemical2Color;
	
	public String bubbles;
	public String smoke;
	public String explosion;
	public int delay;
	public String sedColor;
	public String sedResult;
	public String sedForm;
	
	public String shake;
	public String light;
	
	public String productName;
	public String productFormula;
	public String productColor;
	
	private float redColorCoords[] = {
		.93f, 140/255f, .70f, 1.0f, //bottom right
		245/255f, 0/255f, 0/255f, 1.0f, //top left							
		 245/255f, 0/255f, 0/255f, 1.0f, 
		 .93f, 140/255f, .70f, 1.0f }; //top right
	
	private float greenColorCoords [] = {
		189/255f, 252/255f, 201/255f, 1.0f,
		61/255f, 145/255f, 64/255f, 1.0f,
		61/255f, 145/255f, 64/255f, 1.0f,
		189/255f, 252/255f, 201/255f, 1.0f };
	
	private float blueColorCoords[] = {
		0.0f, 191/255f, 200/255f, 1.0f,
		65/255f, 105/255f, 255/255f, 1.0f,
		65/255f, 105/255f, 255/255, 1.0f,
		0.0f, 191/255f, 200/255f, 1.0f
		 };
	
	//Dictionary for all the color values
	public HashMap<String, float []> colorValues = new HashMap<String, float[]>();
	
	public JSONValues(JSONObject obj) throws JSONException {
		
		chemical1Name = obj.getJSONObject("Reactants").getJSONObject("Reactant A").getString("name");
		chemical1Formula = obj.getJSONObject("Reactants").getJSONObject("Reactant A").getString("formula");
		chemical1Color = obj.getJSONObject("Reactants").getJSONObject("Reactant A").getString("color");
		
		chemical2Name = obj.getJSONObject("Reactants").getJSONObject("Reactant B").getString("name");
		chemical2Formula = obj.getJSONObject("Reactants").getJSONObject("Reactant B").getString("formula");
		chemical2Color = obj.getJSONObject("Reactants").getJSONObject("Reactant B").getString("color");
		
		bubbles = obj.getJSONObject("Interaction").getJSONObject("Observations").getString("bubbles");
		smoke = obj.getJSONObject("Interaction").getJSONObject("Observations").getString("smoke");
		explosion = obj.getJSONObject("Interaction").getJSONObject("Observations").getString("explosion");
		delay = obj.getJSONObject("Interaction").getJSONObject("Observations").getInt("delay");
			
		sedColor = obj.getJSONObject("Interaction").getJSONObject("Observations").getJSONObject("Sedimentation").getString("color");
		sedResult = obj.getJSONObject("Interaction").getJSONObject("Observations").getJSONObject("Sedimentation").getString("result");
		sedForm = obj.getJSONObject("Interaction").getJSONObject("Observations").getJSONObject("Sedimentation").getString("form");
		
		light = obj.getJSONObject("Interaction").getJSONObject("Catalysis").getString("light");
		shake = obj.getJSONObject("Interaction").getJSONObject("Catalysis").getString("shake");
		
		productName = obj.getJSONObject("Interaction").getJSONObject("Product").getString("name");
		productFormula = obj.getJSONObject("Interaction").getJSONObject("Product").getString("formula");
		productColor = obj.getJSONObject("Interaction").getJSONObject("Product").getString("color");
		
		colorValues.put("red", redColorCoords); 
		colorValues.put("green", greenColorCoords);
		colorValues.put("blue", blueColorCoords);
	
	}
	
}
