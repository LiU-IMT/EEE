package se.liu.imt.mi.eee.utils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;

public class CacheControlSettingsFilter extends Filter {

	private List<CacheDirective> cacheDirectives;
	private int expirationInSecondsFromNow;

	public CacheControlSettingsFilter(Context context, Restlet next, List<CacheDirective> cacheDirectives, int expirationInSecondsFromNow) {
		super(context, next);
		this.cacheDirectives = cacheDirectives;
		this.expirationInSecondsFromNow = expirationInSecondsFromNow;
	}

	@Override
	protected void afterHandle(Request request, Response response) {
		response.setCacheDirectives(cacheDirectives);
		//Date expirationDate = new Date(System.currentTimeMillis()+expirationInSecondsFromNow*1000);
		//response.getEntity().setExpirationDate(expirationDate);
		super.afterHandle(request, response);
	}
	
	

}
