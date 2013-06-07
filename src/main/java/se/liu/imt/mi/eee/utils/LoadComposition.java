package se.liu.imt.mi.eee.utils;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openehr.schemas.v1.COMPOSITION;
import org.openehr.schemas.v1.CompositionDocument;
import org.w3c.dom.Node;

import se.liu.imt.mi.eee.structure.XmlHelper;

/**
 * This class is used both from "se.liu.imt.mi.eee.utils.LoadPatientData.java" and from test files.
 * 
 * @author Daniel
 */

public class LoadComposition {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	public static COMPOSITION loadCompositionFromFile(String filename) throws Exception{
		// CompositionDocument compDoc = CompositionDocument.Factory.parse( fromClasspath("composition.xml"));	
		File file = new File(filename);
		return loadCompositionFromFile(file);
	}
	
	public static COMPOSITION loadCompositionFromFile(File file)
	throws XmlException, IOException {
		System.out.println("LoadComposition.loadCompositionFromFile(): "+file.getAbsolutePath());
//		CompositionDocument compositionDocument = CompositionDocument.Factory.newInstance(XmlHelper.getXMLoptions());
//		compositionDocument = CompositionDocument.Factory.parse(file);
		
		XmlOptions opts = new XmlOptions();
		COMPOSITION comp = COMPOSITION.Factory.parse(file);
		return comp;
		
	}
}
