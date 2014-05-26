package org.music_encoding;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class GuidelineRequest implements Processable {

	private final HashMap<String, String> fields;

	public GuidelineRequest(HashMap<String, String> fields) {
		this.fields = fields;
	}

	public byte[] run() throws Exception {
		
		String format = fields.get("guidelinesFormat");
		String source = fields.get("guidelinesSource");
		
		String sourceFile = fields.get("sourceG");

		if(source.equals("mei2013")) {
			sourceFile = MEIWebService.MEI_DIR + "/tags/MEI2013_v2.1.0/source/driver_canonicalized.xml";
			fields.put("sourceGName", "driver_canonicalized.xml");
			fields.put("sourceG", sourceFile);
		
		}else if(source.equals("mei2012")) {
			sourceFile = MEIWebService.MEI_DIR + "/tags/MEI2012_v2.0.0/source/driver_canonicalized.xml";
			fields.put("sourceGName", "driver_canonicalized.xml");
			fields.put("sourceG", sourceFile);
		
		}else if(source.equals("dev")) {
			sourceFile = MEIWebService.MEI_DIR + "/trunk/source/driver_canonicalized.xml";
			fields.put("sourceGName", "driver_canonicalized.xml");
			fields.put("sourceG", sourceFile);
		
		}
		
		if(format.equals("html_zip")) {
			String html = transformToGuidelines(sourceFile, fields.get("tmpDir"));
			
			if(!source.equals("local")) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream out = new ZipOutputStream(baos);
				
				out.putNextEntry(new ZipEntry("guidelines.html"));
				out.write(html.getBytes("UTF-8"));
				out.closeEntry();
				
				String parent = new File(sourceFile).getParentFile().getAbsolutePath();
				File imgDir = new File(parent + "/guidelines/Images");
				addImgEntry(imgDir, "guidelines/Images/", out);

				out.close();
				baos.close();

		        return baos.toByteArray();
			}
			
			return html.getBytes("UTF-8");
		}

		if(format.equals("pdf"))
			;//TODO:return transformToGuidelines(sourceFile);

		return new byte[0];
	}
	
	private void addImgEntry(File path, String prefix, ZipOutputStream out) throws IOException {
		
		File[] imgs = path.listFiles();
		
		for(File img : imgs) {
			
			if(!img.isDirectory()) {
			
				out.putNextEntry(new ZipEntry(prefix + img.getName()));
	
				FileInputStream fi = new FileInputStream(img);
	            BufferedInputStream bif = new BufferedInputStream(fi, 2048);
	            byte[] data = new byte[2048];
	            int count;
	            while((count = bif.read(data, 0, 2048)) != -1) {
	            	out.write(data, 0, count);
	            }
	            bif.close();
				
				out.closeEntry();
			
			}else {
				addImgEntry(img, prefix + img.getName() + "/", out);
			}
		}
	}
	
	private String transformToGuidelines(String source, String tmpDir) throws IOException {

		File buildFile = new File(MEIWebService.TEI_DIR + "/xhtml2/build-to.xml");
		
		Project p = new Project();
		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
		p.setProperty("inputFile", source);
		p.setProperty("outputFile", tmpDir + "/source_.html");
		p.setProperty("profile", "mei-guidelines");
		
		p.init();
		
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		
		helper.parse(p, buildFile);
		
		p.executeTarget(p.getDefaultTarget());
		
		File guidelines = new File(tmpDir + "/source_.html");
		
		FileInputStream fileInputStream = new FileInputStream(guidelines);
        byte[] data = new byte[(int) guidelines.length()];
        fileInputStream.read(data);
        fileInputStream.close();
        
        return new String(data, "UTF-8");
	}

	public static String getFileName(HashMap<String, String> fields) {
		
		String format = fields.get("guidelinesFormat");
		String source = fields.get("guidelinesSource");
		String sourceName = (source.equals("local")?fields.get("sourceGName"):"guidelines.xml");
		sourceName = (sourceName.endsWith(".xml")?sourceName.substring(0, sourceName.length() - 4):sourceName);
		
		if(format.equals("html_zip") && source.equals("local"))
			return sourceName + ".html";
		
		if(format.equals("html_zip") && !source.equals("local"))
			return sourceName + ".zip";

		if(format.equals("pdf"))
			return sourceName + ".pdf";
		
		return "unknown";
	}
}
