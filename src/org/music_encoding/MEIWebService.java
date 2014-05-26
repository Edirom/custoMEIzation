package org.music_encoding;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class MEIWebService
 */
public class MEIWebService extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	static final String TEI_DIR = "/var/opt/tei";
	static final String MEI_DIR = "/var/opt/mei";
	private static final String TEMP_DIR = "/var/tmp";
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MEIWebService() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String redirect = request.getRequestURL().toString().replaceAll("process$", "");
		response.sendRedirect(redirect);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		if(isMultipart) {
			
			//TODO: Fehlerbehandlung
			//TODO: Documentation

			ServletOutputStream sos = response.getOutputStream();
			byte[] result = new byte[0];
			HashMap<String, String> fields = null;
			
			String tmpDir = TEMP_DIR + "/MEIWebService_" + System.currentTimeMillis();
			new File(tmpDir).mkdirs();
			
			try {
				fields = parseUploadedFormFields(request, tmpDir);
				fields.put("tmpDir", tmpDir);

				result = process(fields); 

				response.setContentType("application/x-unknown; charset=utf-8");
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Content-Disposition", "attachment; filename=" + getFileName(fields));
				
				sos.write(result);

			}catch (Exception e) {
				response.setContentType("text/plain");
				sos.write(e.getMessage().getBytes());
			}finally {
				cleanup(tmpDir);
			}
			
			sos.flush();
			sos.close();
			
		}else {
			String redirect = request.getRequestURL().toString().replaceAll("process$", "");
			response.sendRedirect(redirect);
		}
	}

	private byte[] process(HashMap<String, String> fields) throws Exception {

		String outputType = fields.get("outputType");
		
		if(!isValidOutputType(outputType)) throw new Exception("Unknown output type: " + outputType);
		
		if(outputType.equals("schema"))
			return new SchemaRequest(fields).run();
		
		return new GuidelineRequest(fields).run();
	}

	private boolean isValidOutputType(String outputType) {
		return (outputType != null && (outputType.equals("schema") || outputType.equals("guidelines")));
	}

	private void cleanup(String path) {
		
		File f = new File(path);
		
		if(!f.exists()) return;

		if(f.isFile()) f.delete();
		
		if(f.isDirectory()) {
			File[] files = f.listFiles();
			for(int i = 0; i < files.length; i++)
				cleanup(files[i].getAbsolutePath());
			
			f.delete();
		}
	}

	private HashMap<String, String> parseUploadedFormFields(HttpServletRequest request, String tmpDir) throws Exception {
		
		HashMap<String, String> map = new HashMap<String, String>();
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(0);
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		List<?> items = null;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		
		if(items != null) {
			Iterator<?> iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();

			    if (!item.isFormField()) {
			        if(item.getFieldName().equals("source")) {
			        	
			        	String source = tmpDir + "/" + "source.xml";
			        	item.write(new File(source));
			        	
			        	map.put("source", source);
			        	map.put("sourceName", item.getName());
			        	
			        }else if(item.getFieldName().equals("customization")) {
			        	
			        	String customization = tmpDir + "/" + "customization.xml";
			        	item.write(new File(customization));
			        	
			        	map.put("customization", customization);
			        	map.put("customizationName", item.getName());
			        
			        }else if(item.getFieldName().equals("sourceG")) {
			        	
			        	String source = tmpDir + "/" + "sourceG.xml";
			        	item.write(new File(source));
			        	
			        	map.put("sourceG", source);
			        	map.put("sourceGName", item.getName());
			        	
			        }

			    }else {
			    	String name = item.getFieldName();
			        String value = item.getString();
			        
			        map.put(name, value);
			    }
			}
		}
		
		return map;
	}
	
	private String getFileName(HashMap<String, String> fields) {
		
		String outputType = fields.get("outputType");
		
		if(outputType.equals("schema"))
			return SchemaRequest.getFileName(fields);
		
		if(outputType.equals("guidelines"))
			return GuidelineRequest.getFileName(fields);

		return "unknown";
	}

}
