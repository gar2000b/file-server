package com.onlineinteract.fileserver.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@EnableAutoConfiguration
public class FileController {

	private static final int CHUNK = 2000000000;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping("/file/{fileName:.+}")
	public void download(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("fileName") String fileName) throws IOException {
		response.setContentType("application/octet-stream");
		response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
		
		logger.info("Requesting file: " + fileName);

		File file = new File("/files/" + fileName);
		FileInputStream fileIn = new FileInputStream(file);
		ServletOutputStream out = response.getOutputStream();

		if (file.length() > CHUNK) {
			long noOfChunks = (file.length() / CHUNK) + 1;
			long lastChunkSize = file.length() - (CHUNK * (noOfChunks - 1));
			logger.info("File: " + fileName + " is greater then 2Gb and has " + noOfChunks + " no of chunks with the last chunk size of " + lastChunkSize + ", Fetching...");
			for (int i = 0; i < noOfChunks; i++) {
				byte[] outputByte;
				if (i == (noOfChunks - 1)) {
					outputByte = new byte[(int) lastChunkSize];
					fileIn.read(outputByte, 0, (int) lastChunkSize);
					out.write(outputByte, 0, (int) lastChunkSize);
				} else {
					outputByte = new byte[CHUNK];
					fileIn.read(outputByte, 0, CHUNK);
					out.write(outputByte, 0, CHUNK);
				}
			}
		} else {
			logger.info("File: " + fileName + " is less then 2Gb. Fetching...");
			byte[] outputByte = new byte[(int) file.length()];
			while (fileIn.read(outputByte, 0, (int) file.length()) != -1) {
				out.write(outputByte, 0, (int) file.length());
			}
		}

		fileIn.close();
		out.close();
	}
}
