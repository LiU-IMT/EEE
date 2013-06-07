package se.liu.imt.mi.eee.ehr.res;

import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import se.liu.imt.mi.eee.db.EHRDatabaseReadInterface;
import se.liu.imt.mi.eee.structure.EEEConstants;

public class CurrentDatabaseTimeResource extends WadlServerResource implements EEEConstants {

	EHRDatabaseReadInterface<?,?> dbConnection;
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		// Get the db handle from context (used both for POST & GET handling)
		dbConnection = (EHRDatabaseReadInterface<?,?>) getContext().getAttributes().get(KEY_TO_BASIC_DB_READER);
		this.setName("Current Database Time");
		this.setDescription("This resource returns the current time and date in the underlying database " +
				"as a ISO DateTime string. (Example: 2011-03-28T17:32:34.498+02:00)");
	}
	
	@Get("text/plain")
	public String getXMLDateTimeString() {
		try {
			return dbConnection.getCurrentDatabaseTimeAsISODateTimeString();
		} catch (Exception e) {
			String errorString = "The current time could not be fetched from database server.\nError:\n"+e.getLocalizedMessage();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, errorString, e);
		}
	}

}
