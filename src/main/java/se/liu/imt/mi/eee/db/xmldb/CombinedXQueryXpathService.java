package se.liu.imt.mi.eee.db.xmldb;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.CompiledExpression;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.Service;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XQueryService;

/**
 * Ugly testhack to get around limitation in the XML:DB implementation of BaseX in a testcase. Not for production use.
 * @author erisu
 */
public class CombinedXQueryXpathService implements XQueryService, XPathQueryService {

	public class MyCompiledExpression implements CompiledExpression {
		String query;

		public MyCompiledExpression(String query) {
			super();
			this.query = query;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public void reset() {
		}
	}

	private XPathQueryService xqsSpecial;
	private HashMap<String, Object> variableMap;

	public CombinedXQueryXpathService(XPathQueryService xqsSpecial) {
		this.xqsSpecial = xqsSpecial;
		variableMap = new HashMap<String, Object>();
	}

	public String getName() throws XMLDBException {
		return xqsSpecial.getName();
	}

	public String getVersion() throws XMLDBException {
		return xqsSpecial.getVersion();
	}

	public void setCollection(Collection arg0) throws XMLDBException {
		xqsSpecial.setCollection(arg0);
	}

	public String getProperty(String arg0) throws XMLDBException {
		return xqsSpecial.getProperty(arg0);
	}

	public void setProperty(String arg0, String arg1) throws XMLDBException {
		xqsSpecial.setProperty(arg0, arg1);
	}

	public void clearNamespaces() throws XMLDBException {
		xqsSpecial.clearNamespaces();		
	}

	public CompiledExpression compile(String arg0) throws XMLDBException {
		//throw new NotImplementedException();
		return new MyCompiledExpression(arg0);
	}

	public void declareVariable(String arg0, Object arg1) throws XMLDBException {
		variableMap.put(arg0, arg1);
	}

	public ResourceSet execute(CompiledExpression arg0) throws XMLDBException {
		MyCompiledExpression mce = (MyCompiledExpression) arg0;
		Set<Entry<String, Object>> entryset = variableMap.entrySet();
		String query = mce.getQuery();
		for (Entry<String, Object> entry : entryset) {
			query = query.replaceAll('$'+entry.getKey(), entry.getValue().toString());
		}
		return query(query);
	}

	public String getNamespace(String arg0) throws XMLDBException {
		return xqsSpecial.getNamespace(arg0);
	}

	public ResourceSet query(String arg0) throws XMLDBException {
		return xqsSpecial.query(arg0);
	}

	public ResourceSet queryResource(String arg0, String arg1)
			throws XMLDBException {
		return xqsSpecial.queryResource(arg0, arg1);
	}

	public void removeNamespace(String arg0) throws XMLDBException {
		xqsSpecial.removeNamespace(arg0);		
	}

	public void setModuleLoadPath(String arg0) {
 		throw new NotImplementedException();
	}

	public void setNamespace(String arg0, String arg1) throws XMLDBException {
		xqsSpecial.setNamespace(arg0, arg1)	;	
	}

	public void setXPathCompatibility(boolean arg0) {
 		throw new NotImplementedException();
	}	
	
}