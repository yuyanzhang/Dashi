package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Restaurant;

import org.json.JSONArray;
import org.json.JSONObject;

import yelp.YelpAPI;

public class DBConnection {
	
	private static final int MAX_RECOMMENDED_RESTAURANTS = 10;
	
	private Connection conn;

	public DBConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(DBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void executeUpdateStatement(String query) {
		if (conn == null) {
			return;
		}
		try {
			Statement stmt = conn.createStatement();
			System.out.println("\nDBConnection executing query:\n" + query);
			stmt.executeUpdate(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ResultSet executeFetchStatement(String query) {
		if (conn == null) {
			return null;
		}
		try {
			Statement stmt = conn.createStatement();
			System.out.println("\nDBConnection executing query:\n" + query);
			return stmt.executeQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Set<String> getCategories(String businessId) {
	   	 try {
	   		 String sql = "SELECT categories from restaurants WHERE business_id='" + businessId + "'";
	   		 ResultSet rs = executeFetchStatement(sql);
	   		 if (rs.next()) {
	   			 Set<String> set = new HashSet<>();
	   			 String[] categories = rs.getString("categories").split(",");
	   			 for (String category : categories) {
	   				 // ' Japanese ' -> 'Japanese'
	   				 set.add(category.trim());
	   			 }
	   			 return set;
	   		 }
	   	 } catch (Exception e) {
	   		 System.out.println(e.getMessage());
	   	 }
	   	 return new HashSet<String>();
	    }

	private Set<String> getBusinessId(String category) {
	   	 // student question
		 Set<String> set = new HashSet<>();
		  	try {
		  		// if category = Chinese, categories = Chinese, Korean, Japanese, it's a match
		  		String sql = "SELECT business_id from restaurants WHERE categories LIKE '%"
		  				+ category + "%'";
		  		ResultSet rs = executeFetchStatement(sql);
		  		while (rs.next()) {
		  			String businessId = rs.getString("business_id");
		  			set.add(businessId);
		  		}
		  	} catch (Exception e) {
		  		System.out.println(e.getMessage());
		  	}
		  	return set;

	    }    
		public JSONObject getRestaurantsById(String businessId, boolean isVisited) {
	   	 try {
	   		 String sql = "SELECT * from "
	   				 + "restaurants where business_id='" + businessId + "'" + " ORDER BY stars DESC";
	   		 ResultSet rs = executeFetchStatement(sql);
	   		 if (rs.next()) {
	   			 Restaurant restaurant = new Restaurant(
	   					 rs.getString("business_id"),
	   					 rs.getString("categories"),
	   					 rs.getString("name"),
	   					 rs.getString("city"),
	   					 rs.getString("state"),
	   					 rs.getFloat("stars"),
	   					 rs.getString("full_address"),
	   					 rs.getFloat("latitude"),
	   					 rs.getFloat("longitude"),
	   					 rs.getString("image_url"),
	   					 rs.getString("url"));
	   			 JSONObject obj = restaurant.toJSONObject();
	   			 obj.put("is_visited", isVisited);
	   			 return obj;
	   		 }
	   	 } catch (Exception e) { /* report an error */
	   		 System.out.println(e.getMessage());
	   	 }
	   	 return null;
	    }

	public void SetVisitedRestaurants(String userId, List<String> businessIds) {
		for (String businessId : businessIds) {
			executeUpdateStatement("INSERT INTO history (`user_id`, `business_id`) VALUES (\"" + userId + "\", \""
					+ businessId + "\")");
		}
	}

	public void UnsetVisitedRestaurants(String userId, List<String> businessIds) {
		for (String businessId : businessIds) {
			executeUpdateStatement("DELETE FROM history WHERE `user_id`=\"" + userId + "\" and `business_id` = \""
					+ businessId + "\"");
		}
	}

	public Set<String> getVisitedRestaurants(String userId) {
		Set<String> visitedRestaurants = new HashSet<String>();
		try {
			String sql = "SELECT business_id from history WHERE user_id=" + userId;
			ResultSet rs = executeFetchStatement(sql);
			while (rs.next()) {
				String visitedRestaurant = rs.getString("business_id");
				visitedRestaurants.add(visitedRestaurant);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return visitedRestaurants;
	}
	//
	 public JSONArray RecommendRestaurants(String userId) {
	   	 try {
	   		 Set<String> visitedRestaurants = getVisitedRestaurants(userId);
	   		 Set<String> allCategories = new HashSet<>();// why hashSet?
	   		 for (String restaurant : visitedRestaurants) {
	   			 allCategories.addAll(getCategories(restaurant));
	   		 }
	   		 Set<String> allRestaurants = new HashSet<>();
	   		 for (String category : allCategories) {
	   			 Set<String> set = getBusinessId(category);
	   			 allRestaurants.addAll(set);
	   		 }
	   		 Set<JSONObject> diff = new HashSet<>();
	   		 int count = 0;
	   		 for (String businessId : allRestaurants) {
	   			 // Perform filtering
	   			 if (!visitedRestaurants.contains(businessId)) {
	   				 diff.add(getRestaurantsById(businessId, false));
	   				 count++;
	   				 if (count >= MAX_RECOMMENDED_RESTAURANTS) {
	   					 break;
	   				 }
	   			 }
	   		 } 
	   		 return new JSONArray(diff);
	   	 } catch (Exception e) {
	   		 System.out.println(e.getMessage());
	   	 }
	   	 return null;
	    }

	public JSONArray SearchRestaurants(String userId, double lat, double lon) {
		try {
			YelpAPI api = new YelpAPI();
			JSONObject response = new JSONObject(api.searchForBusinessesByLocation(lat, lon));
			JSONArray array = (JSONArray) response.get("businesses");

			List<JSONObject> list = new ArrayList<>();
			Set<String> visited = getVisitedRestaurants(userId);

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				Restaurant restaurant = new Restaurant(object);
				String businessId = restaurant.getBusinessId();
				String name = restaurant.getName();
				String categories = restaurant.getCategories();
				String city = restaurant.getCity();
				String state = restaurant.getState();
				String fullAddress = restaurant.getFullAddress();
				double stars = restaurant.getStars();
				double latitude = restaurant.getLatitude();
				double longitude = restaurant.getLongitude();
				String imageUrl = restaurant.getImageUrl();
				String url = restaurant.getUrl();
				JSONObject obj = restaurant.toJSONObject();
				if (visited.contains(businessId)) {
					obj.put("is_visited", true);
				} else {
					obj.put("is_visited", false);
				}
				executeUpdateStatement("INSERT IGNORE INTO restaurants " + "VALUES ('" + businessId + "', \"" + name
						+ "\", \"" + categories + "\", \"" + city + "\", \"" + state + "\", " + stars + ", \""
						+ fullAddress + "\", " + latitude + "," + longitude + ",\"" + imageUrl + "\", \"" + url
						+ "\")");
				list.add(obj);

			}
			return new JSONArray(list);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}