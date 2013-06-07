package se.liu.imt.mi.eee.trigger;

import org.restlet.data.Form;

public interface ContributionTrigger<T> {

	public abstract void notifyTriggerHandler(String ehrId, T contribution)
			throws Exception;

	public abstract void notifyTriggerHandler(String ehrId, T contribution,
			Form archetypesUsed) throws Exception;

}