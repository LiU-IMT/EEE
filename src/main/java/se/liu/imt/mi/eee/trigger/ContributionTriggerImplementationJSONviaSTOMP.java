package se.liu.imt.mi.eee.trigger;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import net.ser1.stomp.Client;
import net.ser1.stomp.Stomp;

import org.json.JSONObject;
import org.openehr.rm.common.changecontrol.Contribution;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;


public class ContributionTriggerImplementationJSONviaSTOMP implements ContributionTrigger<Contribution>, EhrMetadataCache {
	
	protected Stomp gozirraStompClient;
	protected int timeoutInMilliseconds;
	protected String destination;
	
	/**
	 * Key is ehr-id, value is laste time that ehr was updated;
	 */
	protected Hashtable<String, Date> ehrLastModifiedCacheTable;
	
	protected Hashtable<String, String> ehrETagCacheTable;
	
	public ContributionTriggerImplementationJSONviaSTOMP(
			Stomp gozirraStompClient, int timeoutInMilliseconds, String destination) throws Exception {
		ehrLastModifiedCacheTable = new Hashtable<String, Date>();
		ehrETagCacheTable = new Hashtable<String, String>();
		// System.out.println("ContributionTriggerImplementationJSONviaSTOMP.ContributionTriggerImplementationJSONviaSTOMP() - constructor 1 - hashtables created");
		this.gozirraStompClient = gozirraStompClient;
		this.timeoutInMilliseconds = timeoutInMilliseconds;
		this.destination = destination;
		initiateCommunication();
	}

	// TODO: possibly optionally store all parameters and implemet method for atempting reconnect if the target STOMP server restarts (and if it is not keeping persistent on disk connections between restarts)
	public ContributionTriggerImplementationJSONviaSTOMP(String server, int port, String login, String passcode, int timeoutInMilliseconds, String destination) throws Exception {
		ehrLastModifiedCacheTable = new Hashtable<String, Date>();
		ehrETagCacheTable = new Hashtable<String, String>();
		// System.out.println("ContributionTriggerImplementationJSONviaSTOMP.ContributionTriggerImplementationJSONviaSTOMP() - constructor 2 - hashtables created");
		this.gozirraStompClient = new Client(server, port, login, passcode);
		this.timeoutInMilliseconds = timeoutInMilliseconds;
		this.destination = destination;
		initiateCommunication();
	}
	
	protected void initiateCommunication()  throws Exception {
		// TODO: Test the connection by publishing a message stating that EEE has started on machine X		
		if (!gozirraStompClient.isConnected()) throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, "The trigger handler is not connected ");
		System.out.println("ContributionTriggerImplementationJSONviaSTOMP.initiateCommunication() - client connected");
	}

	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.trigger.EhrMetadataCache#getLastModified(java.lang.String)
	 */
	public Date getLastModified(String ehr_id){
		return ehrLastModifiedCacheTable.get(ehr_id);
	}
	
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.trigger.EhrMetadataCache#putLastModified(java.lang.String, java.util.Date)
	 */
	public Date putLastModified(String ehr_id, Date modTime){
		return ehrLastModifiedCacheTable.put(ehr_id, modTime);
	}
	
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.trigger.EhrMetadataCache#getEtag(java.lang.String)
	 */
	public String getEtag(String ehr_id) {
		// System.out.println("ContributionTriggerImplementationJSONviaSTOMP.getEtag(ehrETagCacheTable):"+ehrETagCacheTable);
		return ehrETagCacheTable.get(ehr_id);
	}

	
	/* (non-Javadoc)
	 * @see se.liu.imt.mi.eee.trigger.EhrMetadataCache#putEtag(java.lang.String, java.lang.String)
	 */
	public void putEtag(String ehr_id, String eTag) {
		this.ehrETagCacheTable.put(ehr_id, eTag);
	}

	public void notifyTriggerHandler(String ehrId, Contribution contribution, Form archetypesUsed) throws Exception {
		if (contribution == null) throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, "The trigger handler is unavailable ");

		// Construct a reciept id
		String receiptID = contribution.getUid().getValue() +"@"+ System.currentTimeMillis();
		String timeCommitted = contribution.getAudit().getTimeCommitted().getDateTime().toDateTimeISO().toString();
			
		HashMap<String, String> header = new HashMap<String, String>(2);
		header.put("ehr_id", ehrId);
		header.put("contribution_id", contribution.getUid().getValue());
		header.put("time_committed", timeCommitted);
		header.put("receipt", receiptID ); // Asks for receipt from STOMP server

		JSONObject jobj = new JSONObject();
		jobj.put("contribution", contribution.toString());
		
		if (archetypesUsed != null) {
			if (archetypesUsed != null) jobj.put("archetypesUsed", archetypesUsed.getValuesMap());
			header.put("includesArchetypeInfo", "true");
		}
		String mesg = jobj.toString(3); //indent factor = 3	
		
//		System.out.println("ContributionTriggerImplementationJSONviaSTOMP.notifyTriggerHandler() ");
//		System.out.println("Sent headers: "+header.toString());
//		System.out.println("Sent body: "+mesg);
		
		// TODO: possibly enforce receipt checking by using
		gozirraStompClient.send(destination, mesg, header);
		
		boolean acknowledged = gozirraStompClient.waitOnReceipt(receiptID, timeoutInMilliseconds);
		if (!acknowledged) throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, "The trigger handler did not acknowledge the contribution trigger within the (configurable) timout "+timeoutInMilliseconds+"milliseconds");

		// Update cache
		putLastModified(ehrId, contribution.getAudit().getTimeCommitted().getDateTime().toDate());
		putEtag(ehrId, contribution.getUid().getValue());
		
		return;
	}
	
	public void notifyTriggerHandler(String ehrId, Contribution contribution) throws Exception {
		notifyTriggerHandler(ehrId, contribution, null);
	}

	public Stomp getGozirraStompClient() {
		return gozirraStompClient;
	}

	public void setGozirraStompClient(Stomp gozirraStompClient) {
		this.gozirraStompClient = gozirraStompClient;
	}

	public int getTimeoutInMilliseconds() {
		return timeoutInMilliseconds;
	}

	public void setTimeoutInMilliseconds(int timeoutInMilliseconds) {
		this.timeoutInMilliseconds = timeoutInMilliseconds;
	}

	public String getDest() {
		return destination;
	}

	public void setDest(String dest) {
		this.destination = dest;
	}
	
	
	
}
