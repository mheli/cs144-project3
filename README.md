# README #

### What is this repository for? ###

The goal of this project is to provide basic keyword search with a Lucene index and spatial search functionality with a MySQL spatial index on the Ebay data loaded into MySQL in project2. These search functions are published as a Web service using Axis2 and Tomcat. 

### How do I get set up? ###

After loading the Ebay data in project2, run buildSQLIndex.sql to create the spatial index. 
Then do
ant run
in project3-indexer to create the Lucene index.
Next, do
ant build
in project3-searcher to create AuctionSearchService.aar in build/

### Other Info ###
I created one Lucene index on ItemID, Name, and the union of (Name, Categories, Description)
Name and Categories are stored in the index for retrieval later in the search functions.
The union of (Name, Categories, Description) is used only for faster textual search and is not stored in the index.

I created one MySQL spatial index on ItemID, Coordinates
where Coordinates is of type POINT(Latitude, Longitude)
for efficient spatial search.