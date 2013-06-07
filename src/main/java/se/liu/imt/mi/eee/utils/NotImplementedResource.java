package se.liu.imt.mi.eee.utils;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

public class NotImplementedResource extends TraceResource{

		@Override
		protected void doInit() throws ResourceException {
			super.doInit();
			this.myStatus = Status.SERVER_ERROR_NOT_IMPLEMENTED;
			this.setName("Marker for non-implemented resources.");
			this.setDescription("This resource can be called with any HTTP method and will return response with HTTP status (not implemented) and a plain text entity containing debug information. Useful primarily for debugging and development phases.");
		}

}