package model;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

public class stringToJSONArrayTest {

	@Test
	public void StringToJSONATest() {
		JSONArray jsonArray = new JSONArray();
		//assertTrue(jsonArrayStringEquals(jsonArray, Restaurant.stringToJSONArray(",")));
		//jsonArray.put("");
		//assertTrue(jsonArrayStringEquals(jsonArray, Restaurant.stringToJSONArray("")));
		jsonArray.put("Chinese");
		assertTrue(jsonArrayStringEquals(jsonArray, Restaurant.stringToJSONArray(",Chinese")));
		//jsonArray.put("");
		//assertTrue(jsonArrayStringEquals(jsonArray, Restaurant.stringToJSONArray(",Chinese,")));
		//jsonArray.put("Italian");
		//assertTrue(jsonArrayStringEquals(jsonArray, Restaurant.stringToJSONArray("Italian,,Chinese,")));
	}

	boolean jsonArrayStringEquals(JSONArray arr1, JSONArray arr2) {
		Set<String> set1 = new HashSet<String>();
		Set<String> set2 = new HashSet<String>();
		for (int i = 0; i < arr1.length(); i++) {
			try {
				set1.add(arr1.getString(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < arr2.length(); i++) {
			try {
				set2.add(arr2.getString(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return set1.equals(set2);
	}
}
