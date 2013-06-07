package se.liu.imt.mi.eee.structure;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

public class EEEConstantsAsHashMap extends HashMap<String, Object> {

	public EEEConstantsAsHashMap() {
		Field[] fields = EEEConstants.class.getFields();
		for (Field field : fields){
			try {
//				System.out.println("EEEConstantsAsHashMap." +
//						"EEEConstantsAsHashMap() "+field.getName()+" = "+field.get(null));
				put(field.getName(), (String) field.get(null));
			} catch (Exception e) {
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
			}
		}
		
		Class<?>[] classes = EEEConstants.class.getClasses();
		for (Class cl : classes ){
			if (cl.isEnum()) {
				try {
//					System.out.println("EEEConstantsAsHashMap." +
//							"EEEConstantsAsHashMap() "+cl.getSimpleName()+" = "+cl);					
					Object[] enumConstants = cl.getEnumConstants();
					
					HashMap<String, Object> eMap = new HashMap<String, Object>();
					ArrayList<String> eList = new ArrayList<String>();
					for (Object enumConstant: enumConstants){
						eMap.put(enumConstant.toString(), enumConstant);
						eList.add(enumConstant.toString());
//						System.out
//								.println("EEEConstantsAsHashMap.EEEConstantsAsHashMap() ---- "+enumConstant);
					}
					put(cl.getSimpleName()+"_HT", eMap);
					put(cl.getSimpleName(), eList);					
				} catch (Exception e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}			}
		}
		
	}

}
