CREATE TABLE IF NOT EXISTS Location (
	ItemID INT NOT NULL PRIMARY KEY,
	Coordinates POINT NOT NULL,
	FOREIGN KEY (ItemID) REFERENCES Item(ItemID)
) ENGINE=MyISAM;

INSERT INTO Location (ItemID, Coordinates)
SELECT ItemID, POINT(Latitude, Longitude)
FROM Item
WHERE Latitude <> -9001 AND Longitude <> -9001;

CREATE SPATIAL INDEX location_index ON Location (Coordinates);