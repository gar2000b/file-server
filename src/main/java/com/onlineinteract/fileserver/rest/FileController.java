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

	private static final int MINI_CHUNK = 100000;
	private static final int CHUNK = 2000000000;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] args) {
		long noOfChunks = 3591957780l / MINI_CHUNK;
		int remainder = (int) (3591957780l - (noOfChunks * MINI_CHUNK));
		System.out.println(noOfChunks);
		System.out.println(remainder);
		long bytesProcessed = 0;
		for (int i = 0; i < noOfChunks; i++) {
			// System.out.println("p = " + ((i + 1) * MINI_CHUNK) % (MINI_CHUNK * 100));
			if (((i + 1l) * MINI_CHUNK) % (MINI_CHUNK * 100) == 0) {
				bytesProcessed += MINI_CHUNK;
				System.out.println("Processed: " + bytesProcessed + " bytes. i = " + i);
			}
		}
//		System.out.println("t1 = " + ((21499l + 1) * MINI_CHUNK));
//		System.out.println("t2 = " + (MINI_CHUNK * 100));
//		System.out.println("testing = " + (((21499l + 1) * MINI_CHUNK) % (MINI_CHUNK * 100)));
	}

	@RequestMapping("/file/{fileName:.+}")
	public void download(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("fileName") String fileName) {
		try {
			response.setContentType("application/octet-stream");
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

			logger.info("Requesting file: " + fileName);

			File file = new File("/files/" + fileName);
			FileInputStream fileIn = new FileInputStream(file);
			ServletOutputStream out = response.getOutputStream();

			long noOfChunks = file.length() / MINI_CHUNK;
			int remainder = (int) (file.length() - (noOfChunks * MINI_CHUNK));
			logger.info("noOfChunks: " + noOfChunks);
			logger.info("remainder: " + remainder + " bytes");

			byte[] outputByte = new byte[MINI_CHUNK];
			long bytesProcessed = 0;

			for (int i = 0; i < noOfChunks; i++) {
				fileIn.read(outputByte, 0, MINI_CHUNK);
				out.write(outputByte, 0, MINI_CHUNK);
				outputByte = new byte[MINI_CHUNK];
				bytesProcessed += MINI_CHUNK;
				/**
				 * Logs every 10Mb
				 */
				if (((i + 1l) * MINI_CHUNK) % (MINI_CHUNK * 100) == 0) {
					logger.info("Processed: " + bytesProcessed + " bytes");
				}
			}

			logger.info("Processing remainder: " + remainder + " bytes");
			outputByte = new byte[remainder];
			fileIn.read(outputByte, 0, remainder);
			out.write(outputByte, 0, remainder);
			bytesProcessed += remainder;
			logger.info("Processed: " + bytesProcessed + " bytes");

			logger.info("File: " + fileName + " has been successfully sent over the wire");
			fileIn.close();
			out.close();
		} catch (Throwable e) {
			logger.error("Encountered an issue during processing: " + e.getMessage());
		}
	}

	/**
	 * Experimentation method
	 * 
	 * @param request
	 * @param response
	 * @param fileName
	 */
	@RequestMapping("/files/{fileName:.+}")
	public void downloadFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("fileName") String fileName) {
		try {
			response.setContentType("application/octet-stream");
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

			logger.info("Requesting file: " + fileName);

			File file = new File("/files/" + fileName);
			FileInputStream fileIn = new FileInputStream(file);
			ServletOutputStream out = response.getOutputStream();

			byte[] outputByte = new byte[MINI_CHUNK];
			long bytesProcessed = 0;
			while (fileIn.read(outputByte, 0, MINI_CHUNK) != -1) {
				out.write(outputByte, 0, MINI_CHUNK);
				outputByte = new byte[MINI_CHUNK];
				bytesProcessed += MINI_CHUNK;
				logger.info("Processed: " + bytesProcessed + " bytes");
			}

			logger.info("File: " + fileName + " has been successfully sent over the wire");
			fileIn.close();
			out.close();
		} catch (Throwable e) {
			logger.error("Encountered an issue during processing: " + e.getMessage());
		}
	}

	/**
	 * Experimentation method
	 * 
	 * @param request
	 * @param response
	 * @param fileName
	 * @throws IOException
	 */
	@RequestMapping("/f/{fileName:.+}")
	public void downloadF(HttpServletRequest request, HttpServletResponse response,
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
			logger.info("File: " + fileName + " is greater then 2Gb and has " + noOfChunks
					+ " chunks of data with the last chunk size of " + lastChunkSize + ", Fetching...");
			for (int i = 0; i < noOfChunks; i++) {
				byte[] outputByte;
				if (i == (noOfChunks - 1)) {
					logger.info("Writing last chunk...");
					outputByte = new byte[(int) lastChunkSize];
					fileIn.read(outputByte, 0, (int) lastChunkSize);
					out.write(outputByte, 0, (int) lastChunkSize);
				} else {
					logger.info("Writing chunk " + (i + 1) + "...");
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

		logger.info("File: " + fileName + " has been successfully sent over the wire");
		fileIn.close();
		out.close();
	}
}
