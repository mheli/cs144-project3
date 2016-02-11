package edu.ucla.cs.cs144;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Vector;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

import java.lang.StringBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.ucla.cs.cs144.DbManager;
import edu.ucla.cs.cs144.SearchRegion;
import edu.ucla.cs.cs144.SearchResult;

public class AuctionSearch implements IAuctionSearch {

	/* 
         * You will probably have to use JDBC to access MySQL data
         * Lucene IndexSearcher class to lookup Lucene index.
         * Read the corresponding tutorial to learn about how to use these.
         *
	 * You may create helper functions or classes to simplify writing these
	 * methods. Make sure that your helper functions are not public,
         * so that they are not exposed to outside of this class.
         *
         * Any new classes that you create should be part of
         * edu.ucla.cs.cs144 package and their source files should be
         * placed at src/edu/ucla/cs/cs144.
         *
         */

	private class Bid {
		// Bid table
		private String bidderID;
		private Timestamp time;
		private double amount;

		// User table
		private int buyer_rating;
		private String buyer_location;
		private String buyer_country;

		private Bid(String bid, Timestamp t, double a) {
			this.bidderID = bid;
			this.time = t;
			this.amount = a;
		}

	};

	private String formatSpecial(String text){
		String result = text;
		result = result.replaceAll("&", "&amp;");
/*
		result = result.replaceAll("<", "&lt;");
		result = result.replaceAll(">", "&gt;");
		result = result.replaceAll("'", "&apos;");
		result = result.replaceAll("\"", "&quot;");
*/
		return result;
	}

	private String formatMoney(double money){
		DecimalFormat format = new DecimalFormat("$0.00");
		return format.format(money);
	}

	private String formatTime(Timestamp time){
		SimpleDateFormat format = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
		return format.format(time);
	}

	private String formatCoord(double coord){
		DecimalFormat format = new DecimalFormat("##0.######");
		return format.format(coord);
	}

	private String makeTag(String tagName, String content){
		return "<"+tagName+">"+formatSpecial(content)+"</"+tagName+">\n";
	}

	private String makeOptionalTag(String tagName, String content){
		if (content.equals("NULL"))
			return "";
		return makeTag(tagName, content);
	}

	private String makeNullableTag(String tagName, String content){
		if (content.equals("NULL"))
			return "<"+tagName+" />\n";
		else
			return makeTag(tagName, content);
	}

	private String makeMoneyTag(String tagName, double content){
		return "<"+tagName+">"+formatMoney(content)+"</"+tagName+">\n";
	}

	private String makeOptionalMoneyTag(String tagName, double content){
		if (content == -9001)
			return "";
		else
			return makeMoneyTag(tagName, content);
	}

	private String makeIntTag(String tagName, int content){
		return "<"+tagName+">"+Integer.toString(content)+"</"+tagName+">\n";
	}

	private String makeTimeTag(String tagName, Timestamp time){
		return "<"+tagName+">"+formatTime(time)+"</"+tagName+">\n";	
	}

	private String makeExLocationTag(double lat, double lon, String content){
		if (lat == -9001 || lon == -9001)
			return makeTag("Location", content);
		else
			return "<Location Latitude=\""+formatCoord(lat)+"\" "+
		"Longitude=\""+formatCoord(lon)+"\">"+
		formatSpecial(content)+"</Location>\n";	
	}

	public SearchResult[] basicSearch(String query, int numResultsToSkip, 
			int numResultsToReturn) {
		// TODO: Your code here!
        Vector< SearchResult > vResults = new Vector< SearchResult >();

		try{
	        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File("/var/lib/lucene/index1/"))));
	        QueryParser parser = new QueryParser("content", new StandardAnalyzer());

	        Query q = parser.parse(query);        
	        TopDocs topDocs = searcher.search(q, numResultsToSkip + numResultsToReturn);
	        ScoreDoc[] hits = topDocs.scoreDocs;
	        for (int i = numResultsToSkip; i < hits.length; i++) {
	            Document doc = searcher.doc(hits[i].doc);
                vResults.add( new SearchResult(doc.get("id"), doc.get("name")) );
	        }        			
		} catch (IOException ex) {
			System.out.println(ex);
		} catch (ParseException ex) {
			System.out.println(ex);
		}

        SearchResult[] result = new SearchResult[vResults.size()];
        vResults.copyInto(result);

        return result;

	}

	public SearchResult[] spatialSearch(String query, SearchRegion region,
			int numResultsToSkip, int numResultsToReturn) {
		// TODO: Your code here!
		String polygonRegion = "'Polygon((" +
		region.getLx() + " " + region.getLy() + "," +
		region.getRx() + " " + region.getLy() + "," +
		region.getRx() + " " + region.getRy() + "," +
		region.getLx() + " " + region.getRy() + "," +
		region.getLx() + " " + region.getLy() +
		"))'";

		int skip = 0;
		int toSkip = numResultsToSkip;
        Connection conn = null;

        Vector< SearchResult > vResults = new Vector< SearchResult >();
        Vector< String > spacialResults = new Vector< String >();

	    try {
		    conn = DbManager.getConnection(true);
            /* load the driver*/
            Class.forName("com.mysql.jdbc.Driver"); 

            Statement s = conn.createStatement() ;

            ResultSet rs = s.executeQuery(
            "SELECT ItemID FROM Location "+
            "WHERE MBRContains(GeomFromText("+
        	polygonRegion+
        	"),Coordinates);");

            while( rs.next() ){
                spacialResults.add(Integer.toString(rs.getInt("ItemID")));
            }

            Collections.sort(spacialResults);

            while (vResults.size() < numResultsToReturn){
				SearchResult[] basicResults = basicSearch(query, skip, numResultsToReturn);
				if (basicResults.length == 0)
					break;

	            for (int i = 0; i < basicResults.length; i++){
	            	if (spacialResults.contains(basicResults[i].getItemId())){
	            		if (toSkip == 0)
		            		vResults.add(basicResults[i]);
		            	else
		            		toSkip--;
	            	}
	            }
	            skip += numResultsToReturn;
        	}

            /* close the resultset, statement and connection */
            rs.close();
            s.close();
            conn.close();
       } catch (ClassNotFoundException ex){
            System.out.println(ex);
       } catch (SQLException ex){
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ){
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
       } 

        SearchResult[] result = new SearchResult[vResults.size()];
        vResults.copyInto(result);

		return result;
	}

	public String getXMLDataForItemId(String itemId) {
		// TODO: Your code here!
        Connection conn = null;

        // Item table
        String name = "", location = "", country = "", sellerID = "", description = "";
        double currently = -9001, buy_price = -9001, first_bid = -9001, latitude = -9001, longitude = -9001;
        int number_of_bids = -9001;
        Timestamp started = null, ends = null;

        // Category table
        Vector< String > categories = new Vector< String >();

        // Bid table
        Vector< Bid > bids = new Vector < Bid >();

        // User table
        int buyer_rating = -9001, seller_rating = -9001;
        String user_location = "", user_country = "";

	    try {
		    conn = DbManager.getConnection(true);
            /* load the driver*/
            Class.forName("com.mysql.jdbc.Driver"); 

            Statement s = conn.createStatement() ;

            ResultSet rs = s.executeQuery(
            "SELECT * FROM Item "+
            "WHERE ItemID="+itemId+
            ";");

            while( rs.next() ){
            	name = rs.getString("Name");
            	location = rs.getString("Location");
            	country = rs.getString("Country");
            	sellerID = rs.getString("SellerID");
            	description = rs.getString("Description");
            	currently = rs.getDouble("Currently");
            	buy_price = rs.getDouble("Buy_Price");
            	first_bid = rs.getDouble("First_Bid");
            	latitude = rs.getDouble("Latitude");
            	longitude = rs.getDouble("Longitude");
            	number_of_bids = rs.getInt("Number_of_Bids");
            	started = rs.getTimestamp("Started");
            	ends = rs.getTimestamp("Ends");
            }
            if (name.equals(""))
            {
            	return "";
            }

			rs = s.executeQuery(
            "SELECT * FROM Category "+
            "WHERE ItemID="+itemId+
            ";");
            while( rs.next() ){
            	categories.add(rs.getString("Category"));
            }

            rs = s.executeQuery(
            "SELECT * FROM Bid "+
            "WHERE ItemID="+itemId+" "+
            "ORDER BY Time;");
            while( rs.next() ){
            	bids.add(new Bid(
            			rs.getString("BidderID"),
            			rs.getTimestamp("Time"),
            			rs.getDouble("Amount")));
            }

           	for (int i = 0; i < bids.size(); i++){
           		Bid current = bids.get(i);
	            rs = s.executeQuery(
	            "SELECT * FROM User "+
	            "WHERE UserID='"+current.bidderID+"';");
	            while( rs.next() ){
	            	current.buyer_rating = rs.getInt("Buyer_Rating");
	            	current.buyer_country = rs.getString("Country");
	            	current.buyer_location = rs.getString("Location");
	            }
        	}

            rs = s.executeQuery(
            "SELECT * FROM User "+
            "WHERE UserID='"+sellerID+"';");
            while( rs.next() ){
            	buyer_rating = rs.getInt("Buyer_Rating");
            	seller_rating = rs.getInt("Seller_Rating");
            	user_location = rs.getString("Location");
            	user_country = rs.getString("Country");
            }

            /* close the resultset, statement and connection */
            rs.close();
            s.close();
            conn.close();
       } catch (ClassNotFoundException ex){
            System.out.println(ex);
       } catch (SQLException ex){
            System.out.println("SQLException caught");
            System.out.println("---");
            while ( ex != null ){
                System.out.println("Message   : " + ex.getMessage());
                System.out.println("SQLState  : " + ex.getSQLState());
                System.out.println("ErrorCode : " + ex.getErrorCode());
                System.out.println("---");
                ex = ex.getNextException();
            }
       } 

		StringBuilder xml = new StringBuilder();
		xml.append("<Item ItemID=\"" + itemId + "\">\n");
		xml.append(makeTag("Name", name));
		for (int i = 0; i < categories.size(); i++){
			xml.append(makeTag("Category", categories.get(i)));
		}
		xml.append(makeMoneyTag("Currently", currently));
		xml.append(makeOptionalMoneyTag("Buy_Price", buy_price));
		xml.append(makeMoneyTag("First_Bid", first_bid));
		xml.append(makeIntTag("Number_of_Bids", number_of_bids));
		if (bids.size() == 0)
			xml.append("<Bids />\n");
		else{
			xml.append("<Bids>\n");
			for (int i = 0; i < bids.size(); i++){
				Bid current = bids.get(i);
				xml.append("<Bid>\n");
				if (current.buyer_location.equals("NULL") && current.buyer_country.equals("NULL")){
					xml.append(
						"<Bidder Rating=\""+Integer.toString(current.buyer_rating)
						+"\" UserID=\""+current.bidderID+"\" />\n");
				}
				else {
					xml.append(
						"<Bidder Rating=\""+Integer.toString(current.buyer_rating)
						+"\" UserID=\""+current.bidderID+"\">\n");
					xml.append(makeOptionalTag("Location", current.buyer_location));
					xml.append(makeOptionalTag("Country", current.buyer_country));
					xml.append("</Bidder>\n");
				}
				xml.append(makeTimeTag("Time", current.time));
				xml.append(makeMoneyTag("Amount", current.amount));
				xml.append("</Bid>\n");
			}
			xml.append("</Bids>\n");
		}
		xml.append(makeExLocationTag(latitude, longitude, location));
		xml.append(makeTag("Country", country));
		xml.append(makeTimeTag("Started", started));
		xml.append(makeTimeTag("Ends", ends));
		xml.append("<Seller Rating=\""+Integer.toString(seller_rating)+
			" UserID=\""+sellerID+"\" />\n");
		xml.append(makeNullableTag("Description", description));
		xml.append("</Item>\n");

		return xml.toString();
	}
	
	public String echo(String message) {
		return message;
	}

}
