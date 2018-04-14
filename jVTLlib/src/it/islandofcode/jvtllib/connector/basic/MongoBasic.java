package it.islandofcode.jvtllib.connector.basic;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import it.islandofcode.jvtllib.connector.IConnector;
import it.islandofcode.jvtllib.model.DataPoint;
import it.islandofcode.jvtllib.model.DataSet;
import it.islandofcode.jvtllib.model.DataStructure;
import it.islandofcode.jvtllib.model.Scalar;
import it.islandofcode.jvtllib.model.VTLObj;

/**
 * @author Pier Riccardo Monzo
 */
public class MongoBasic implements IConnector {
	MongoClient MC;
	String database;
	String table;
	
	public MongoBasic(String IP, int port, String db) {
		//se IP/porta non specificato o fuori specifica, vai di default
		if(IP==null || IP.isEmpty())
			IP="127.0.0.1";
		if(port<=0 || port>65535)
			port=27017;
		
		MC = new MongoClient(IP,port);
		this.database = db;
	}

	/* (non-Javadoc)
	 * @see it.islandofcode.jvtllib.connector.IConnector#get(java.lang.String)
	 */
	@Override
	public DataSet get(String location) {
		this.table = location;
		DataSet ds = null;
		
		if(this.checkStatus()) {
			MongoDatabase db = MC.getDatabase(this.database);
			MongoCollection<Document> table = db.getCollection(this.table);
			
			Document first = table.find().first();
			DataStructure dstr = new DataStructure(location+"_dstr");
			for(String K : first.keySet()) {
				if(K.equals("_id"))
					continue;
				dstr.putComponent(
						K,
						new Scalar(Scalar.SCALARTYPE.String),
						DataStructure.type.Identifier
						);
			}
			
			ds = new DataSet(location,this.database,dstr,true);
			for(Document D : table.find()) {
				DataPoint dp = new DataPoint();
				for(String I : D.keySet()) {
					if(I.equals("_id"))
						continue;
					dp.setValue(I, new Scalar(""+D.get(I)));
				}
				ds.setPoint(dp);
			}
			
		} else
			return null; //se faccio check prima di get, non dovrei MAI arrivare qui.
		
		return ds;
	}

	/* (non-Javadoc)
	 * @see it.islandofcode.jvtllib.connector.IConnector#set(java.lang.String, it.islandofcode.jvtllib.model.DataSet)
	 */
	@Override
	public boolean set(String location, DataSet data) {
		if(this.checkStatus()) {
			//esiste, sovrascrivo?
		} else {
			//non esiste, lo creo.
			MongoDatabase db = MC.getDatabase(location);
		}
		
		
		return false;
	}

	/* (non-Javadoc)
	 * @see it.islandofcode.jvtllib.connector.IConnector#checkStatus()
	 */
	@Override
	public boolean checkStatus() {
		for(String N : MC.listDatabaseNames()) {
			if(N.equals(this.database))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see it.islandofcode.jvtllib.connector.IConnector#getLocation()
	 */
	@Override
	public String getLocation() {
		return this.table;
	}
	
}