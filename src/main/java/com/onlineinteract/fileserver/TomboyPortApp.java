package com.onlineinteract.fileserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Note: ensure the DESTINATION PATH (currently set to desktop/notes) exists.
 * 
 * TomboyPort app seeks to port over all current notes from Tomboy (1.15.9) to Tomdroid (0.7.5) by simply
 * iterating through each file based on main manifest file. If there are duplicates, then those are overridden 
 * with the latest versions.
 * 
 * Once the files have been placed together into a single directory, copy these over to the Tomdroid directory:
 * This PC\Galaxy Note4\Phone\tomdroid - then sync notes on Tomdroid app.
 * 
 * ([A-Za-z0-9]+-{1})+[A-Za-z0-9]+ : regex to find the id portion of: 
 * <note id="779dff95-06c4-411c-a43c-44b7e0e10393" rev="0" />
 * 
 * @author gar20
 *
 */
public class TomboyPortApp {
	private static final String MANIFEST_FILENAME = "manifest.xml";
	private static final String SPLIT = "\"";
	private static final String NOTE_IDENTIFIER = "note id";
	private static final int DIRECTORY_MARKER_100 = 100;
	private static final int DIRECTORY_MARKER_1000 = 1000;
	private static final int DIRECTORY_POSITION = 0;
	private static final int SUB_DIRECTORY_POSITION = 3;
	private static final int FILENAME_POSITION = 1;
	private static final String SEPERATOR = "\\";
	private static final String NOTE_EXTENSION = ".note";
	private static final String TOMBOY_PATH = "C:\\Users\\gar20\\Dropbox\\tomboy";
	private static final String DESTINATION_PATH = "C:\\Users\\gar20\\Desktop\\notes";

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		new TomboyPortApp().port();
	}

	public void port() throws IOException, ParserConfigurationException, SAXException {
		String fileName = TOMBOY_PATH + SEPERATOR + MANIFEST_FILENAME;

		List<String[]> notes = Files.lines(Paths.get(fileName))
				.filter(x -> x.contains(NOTE_IDENTIFIER))
				.map(x -> x.split(SPLIT))
				.collect(Collectors.toList());

		String directory = "0";
		for (String[] values : notes) {
			System.out.println(values[FILENAME_POSITION] + " " + values[SUB_DIRECTORY_POSITION]);
			if (Integer.valueOf(values[SUB_DIRECTORY_POSITION]) >= DIRECTORY_MARKER_1000)
				directory = values[SUB_DIRECTORY_POSITION].charAt(DIRECTORY_POSITION) + "0";
			else if (Integer.valueOf(values[SUB_DIRECTORY_POSITION]) >= DIRECTORY_MARKER_100)
				directory = String.valueOf(values[SUB_DIRECTORY_POSITION].charAt(DIRECTORY_POSITION));
			copyFile(directory, values[SUB_DIRECTORY_POSITION], values[FILENAME_POSITION] + NOTE_EXTENSION);
			// processFile(DESTINATION_PATH + SEPERATOR + values[FILENAME_POSITION] + NOTE_EXTENSION);
		}
	}
	
	/*
	 * No longer required at this time as creating a single directory of .notes is all that is required before copying over to Android device.
	 * 
	 * Tomboy 1.15.9 and Tomdroid 0.7.5 notes are compatible.Os
	 * 
	 * public void processFile(String fileToProcess) throws
	 * ParserConfigurationException, SAXException, IOException {
	 * System.out.println("Processing: " + fileToProcess); File file = new
	 * File(fileToProcess); DocumentBuilderFactory dbf =
	 * DocumentBuilderFactory.newInstance(); DocumentBuilder db =
	 * dbf.newDocumentBuilder(); Document doc = db.parse(file);
	 * doc.getDocumentElement().normalize(); System.out.println("Root element: " +
	 * doc.getDocumentElement().getNodeName()); NodeList nodeList =
	 * doc.getElementsByTagName("title"); Node titleNode = nodeList.item(0);
	 * System.out.println("Title: " + titleNode.getTextContent()); nodeList =
	 * doc.getElementsByTagName("note-content"); Node contentNode =
	 * nodeList.item(0); System.out.println("Content: " +
	 * contentNode.getTextContent()); createTextFile(titleNode.getTextContent(),
	 * contentNode.getTextContent()); System.out.println(); }
	 * 
	 * public void createTextFile(String fileName, String content) { File file = new
	 * File(DESTINATION_PATH + SEPERATOR + "text-files" + SEPERATOR + fileName);
	 * 
	 * }
	 */
	
	public void copyFile(String directory, String subDirectory, String filename) {
		try {
			File f = new File(Paths.get(DESTINATION_PATH + SEPERATOR + filename).toString());
			if(f.exists())
				f.delete();
			Files.copy(Paths.get(TOMBOY_PATH + SEPERATOR + directory + SEPERATOR + subDirectory + SEPERATOR + filename), Paths.get(DESTINATION_PATH + SEPERATOR + filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
