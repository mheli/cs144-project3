package edu.ucla.cs.cs144;

import java.util.Calendar;
import java.util.Date;

import edu.ucla.cs.cs144.AuctionSearch;
import edu.ucla.cs.cs144.SearchRegion;
import edu.ucla.cs.cs144.SearchResult;

public class AuctionSearchTest {
	public static void main(String[] args1)
	{
		AuctionSearch as = new AuctionSearch();

		String message = "Test message";
		String reply = as.echo(message);
		System.out.println("Reply: " + reply);
		
		String query = "superman";
		SearchResult[] basicResults = as.basicSearch(query, 0, 20);
		System.out.println("Basic Seacrh Query: " + query);
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}
		
		SearchRegion region =
		    new SearchRegion(33.774, -118.63, 34.201, -117.38); 
		SearchResult[] spatialResults = as.spatialSearch("camera", region, 0, 20);
		System.out.println("Spatial Seacrh");
		System.out.println("Received " + spatialResults.length + " results");
		for(SearchResult result : spatialResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}
		
		String itemId = "1497595357";
		String item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// Add your own test here
		
		// 68
		query = "superman";
		basicResults = as.basicSearch(query, 0, 70);
		System.out.println("Basic Seacrh Query: " + query);
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}

		// 2
		query = "superman";
		basicResults = as.basicSearch(query, 66, 70);
		System.out.println("Basic Seacrh Query: " + query);
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}

		// 2
		query = "superman";
		basicResults = as.basicSearch(query, 0, 2);
		System.out.println("Basic Seacrh Query: " + query);
		System.out.println("Received " + basicResults.length + " results");
		for(SearchResult result : basicResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}

		// 1462
		query = "kitchenware";
		basicResults = as.basicSearch(query, 0, 1465);
		System.out.println("Basic Seacrh Query: " + query);
		System.out.println("Received " + basicResults.length + " results");

		// 770
		query = "star trek";
		basicResults = as.basicSearch(query, 0, 780);
		System.out.println("Basic Seacrh Query: " + query);
		System.out.println("Received " + basicResults.length + " results");

		// No Buy price
		itemId = "1679480995";
		item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// No Country and Location for Bidder
		itemId = "1045700537";
		item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// No Country for Bidder
		itemId = "1493984618";
		item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// No Location for Bidder
		itemId = "1494534611";
		item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// No Latitude and Longitude
		itemId = "1679455238";
		item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// No Description
		itemId = "1679233320";
		item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

	}
}
