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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		return "";
	}
	
	public String echo(String message) {
		return message;
	}

}
