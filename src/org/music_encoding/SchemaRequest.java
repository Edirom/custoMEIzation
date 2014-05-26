package org.music_encoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.xml.sax.SAXException;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.parse.sax.SAXParseInputFormat;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.xsd.XsdOutputFormat;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

public class SchemaRequest implements Processable {

	private final HashMap<String, String> fields;

	public SchemaRequest(HashMap<String, String> fields) {
		this.fields = fields;
	}

	public byte[] run() throws Exception {
		
		String format = fields.get("schemaFormat");
		String source = fields.get("schemaSource");
		String customization = fields.get("schemaCustomization");
		
		String sourceFile = fields.get("source");
		String customizationFile = fields.get("customization");

		if(source.equals("mei2013")) {
			sourceFile = MEIWebService.MEI_DIR + "/tags/MEI2013_v2.1.0/source/driver_canonicalized.xml";
			fields.put("sourceName", "driver_canonicalized.xml");
			fields.put("source", sourceFile);
		
		}else if(source.equals("mei2012")) {
			sourceFile = MEIWebService.MEI_DIR + "/tags/MEI2012_v2.0.0/source/driver_canonicalized.xml";
			fields.put("sourceName", "driver_canonicalized.xml");
			fields.put("source", sourceFile);
		
		}else if(source.equals("dev")) {
			sourceFile = MEIWebService.MEI_DIR + "/trunk/source/driver_canonicalized.xml";
			fields.put("sourceName", "driver_canonicalized.xml");
			fields.put("source", sourceFile);
		
		}
		
		if(customization.equals("mei2013")) {
			customizationFile = MEIWebService.MEI_DIR + "/tags/MEI2013_v2.1.0/customizations/mei-all.xml";
			fields.put("customizationName", "mei-all.xml");
			fields.put("customization", customizationFile);
		
		}else if(customization.equals("mei2012")) {
			customizationFile = MEIWebService.MEI_DIR + "/tags/MEI2012_v2.0.0/customizations/mei-all.xml";
			fields.put("customizationName", "mei-all.xml");
			fields.put("customization", customizationFile);
		
		}else if(customization.equals("dev")) {
			customizationFile = MEIWebService.MEI_DIR + "/trunk/customizations/mei-all.xml";
			fields.put("customizationName", "mei-all.xml");
			fields.put("customization", customizationFile);
		
		}
		
		if(format.equals("relax"))
			return transformToRelaxNG(customizationFile, sourceFile, fields.get("tmpDir"));

		if(format.equals("xmlSchema")) {
			transformToRelaxNG(customizationFile, sourceFile, fields.get("tmpDir"));
			byte[] xsdOutput = generateXSDOutput(fields.get("tmpDir"));
			String correctXSDRefs = new String(xsdOutput, "UTF-8");
			correctXSDRefs = correctXSDRefs.replaceAll("=\"xlinkxsd\"", "=\"http://www.w3.org/1999/xlink.xsd\"");
			correctXSDRefs = correctXSDRefs.replaceAll("=\"xmlxsd\"", "=\"http://www.w3.org/2001/xml.xsd\"");
			
			return correctXSDRefs.getBytes("UTF-8");
		}
		
		if(format.equals("compiledODD")) {
			return transformToCompiledODD(customizationFile, sourceFile, fields.get("tmpDir"));
		}

		return new byte[0];
	}
	
	private byte[] transformToCompiledODD(String customization, String source, String tmpDir) throws IOException {
		
		File buildFile = new File(MEIWebService.TEI_DIR + "/relaxng/build-to.xml");
		
		Project p = new Project();
		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
		p.setProperty("inputFile", customization);
		p.setProperty("outputFile", tmpDir + "/customization_.rng");
		p.setProperty("defaultSource", source);
		
		p.init();
		
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		
		helper.parse(p, buildFile);
		
		p.executeTarget("setup");
		p.executeTarget("odd");
		
		File odd = new File(tmpDir + "/temp-dir-for-ant/tmp1.xml");
		
		FileInputStream fileInputStream = new FileInputStream(odd);
        byte[] data = new byte[(int) odd.length()];
        fileInputStream.read(data);
        fileInputStream.close();
        
        return data;
	}

	private byte[] generateXSDOutput(String tmpDir) throws SAXException, IOException, OutputFailedException, InvalidParamsException, InputFailedException {
		
		ErrorHandlerImpl eh = new ErrorHandlerImpl();
		
		InputFormat inFormat = new SAXParseInputFormat();
		OutputFormat of = new XsdOutputFormat();
		
		SchemaCollection sc = inFormat.load(UriOrFile.toUri(new File(tmpDir + "/customization_.rng").getAbsolutePath()), new String[0], "xsd", eh);
		OutputDirectory od = new LocalOutputDirectory(sc.getMainUri(), new File(tmpDir + "/customization_.xsd"), "xsd", "UTF-8", 72, 2);
		
		of.output(sc, od, new String[0], "rng", eh);
		
		File xsd = new File(tmpDir + "/customization_.xsd");
		
		FileInputStream fileInputStream = new FileInputStream(xsd);
        byte[] data = new byte[(int) xsd.length()];
        fileInputStream.read(data);
        fileInputStream.close();
        
        return data;
	}

	private byte[] transformToRelaxNG(String customization, String source, String tmpDir) throws TransformerException, IOException {
		
		File buildFile = new File(MEIWebService.TEI_DIR + "/relaxng/build-to.xml");
		
		Project p = new Project();
		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
		p.setProperty("inputFile", customization);
		p.setProperty("outputFile", tmpDir + "/customization_.rng");
		p.setProperty("defaultSource", source);
		
		p.init();
		
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		
		helper.parse(p, buildFile);
		
		p.executeTarget(p.getDefaultTarget());
		
		File relax = new File(tmpDir + "/customization_.rng");
		
		FileInputStream fileInputStream = new FileInputStream(relax);
        byte[] data = new byte[(int) relax.length()];
        fileInputStream.read(data);
        fileInputStream.close();
        
        return data;
	}

	public static String getFileName(HashMap<String, String> fields)  {
		
		String format = fields.get("schemaFormat");
		String customization = fields.get("schemaCustomization");
		String customizationName = (customization.equals("local")?fields.get("customizationName"):"mei-all");
		customizationName = (customizationName.endsWith(".xml")?customizationName.substring(0, customizationName.length() - 4):customizationName);
		
		if(format.equals("relax"))
			return customizationName + ".rng";

		if(format.equals("xmlSchema"))
			return customizationName + ".xsd";
		
		if(format.equals("compiledODD"))
			return customizationName + "_compiled.xml";
		
		return "unknown";
	}
}
