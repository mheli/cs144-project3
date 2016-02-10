package edu.ucla.cs.cs144;

import java.io.IOException;
import java.io.StringReader;
import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
    
    /** Creates a new instance of Indexer */
    public Indexer() {
    }
 
    private IndexWriter indexWriter = null;
    
    public IndexWriter getIndexWriter(boolean create) throws IOException {
        if (indexWriter == null) {
            Directory indexDir = FSDirectory.open(new File("/var/lib/lucene/index1/"));
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
            indexWriter = new IndexWriter(indexDir, config);
            if (create == true)
                indexWriter.deleteAll();
        }
        return indexWriter;
   }    
   
    public void closeIndexWriter() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
   }

   public void indexItem(ResultSet rs) throws IOException {
        try{
            IndexWriter writer = getIndexWriter(false);
            while( rs.next() ){
                Document doc = new Document();
                int itemID = rs.getInt("ItemID");
                String name = rs.getString("Name");
                String categories = rs.getString("Categories");
                String description = rs.getString("Description");
                doc.add(new IntField("id", itemID, Field.Store.YES));
                doc.add(new TextField("name", name, Field.Store.YES));
                String fullSearchableText = name + " " + categories + " " + description;
                doc.add(new TextField("content", fullSearchableText, Field.Store.NO));
                writer.addDocument(doc);
            }
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
        
    }   

    public void rebuildIndexes() {

        Connection conn = null;

        // create a connection to the database to retrieve Items from MySQL
	try {
	    conn = DbManager.getConnection(true);
	} catch (SQLException ex) {
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


	/*
	 * Add your code here to retrieve Items using the connection
	 * and add corresponding entries to your Lucene inverted indexes.
         *
         * You will have to use JDBC API to retrieve MySQL data from Java.
         * Read our tutorial on JDBC if you do not know how to use JDBC.
         *
         * You will also have to use Lucene IndexWriter and Document
         * classes to create an index and populate it with Items data.
         * Read our tutorial on Lucene as well if you don't know how.
         *
         * As part of this development, you may want to add 
         * new methods and create additional Java classes. 
         * If you create new classes, make sure that
         * the classes become part of "edu.ucla.cs.cs144" package
         * and place your class source files at src/edu/ucla/cs/cs144/.
	 * 
	 */

    
      //
      // Erase existing index
      //
    try {
            getIndexWriter(true);
        } catch (IOException ex){
            System.out.println(ex);
        }


      //
      // Index all entries
      //
    try {
            /* load the driver*/
            Class.forName("com.mysql.jdbc.Driver"); 

            Statement s = conn.createStatement() ;

            ResultSet rs = s.executeQuery(
            "SELECT Item.ItemID, Item.Name, Item.Description, Concated.Categories "+
            "FROM (SELECT DISTINCT Category.ItemID, "+
                "GROUP_CONCAT(DISTINCT category SEPARATOR ' ') AS Categories "+
                "FROM Category GROUP BY ItemID) AS Concated "+
            "INNER JOIN Item ON Item.ItemID = Concated.ItemID;");
            indexItem(rs);

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
       } catch (IOException ex){
            System.out.println(ex);
       }


      //
      // Don't forget to close the index writer when done
      //
   try {
        closeIndexWriter();
   } catch (IOException ex){
        System.out.println(ex);
   }





        // close the database connection
	try {
	    conn.close();
	} catch (SQLException ex) {
	    System.out.println(ex);
	}
    }    

    public static void main(String args[]) {
        Indexer idx = new Indexer();
        idx.rebuildIndexes();
    }   
}
