package com.flex.adapter.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.constants.AppConstants;
import com.flextronics.services.ewi.client.document.FtpInfoWs;

public class FtpClient {
	private Logger LOG = LoggerFactory.getLogger(FtpClient.class);

	FTPClient client = new FTPClient();
	private String ftpHost = "";
	private String user = "";
	private String pwd = "";
	private String port;

	public FtpClient() {
	}

	public FtpClient(FtpInfoWs ftpServer) {
		this.ftpHost = ftpServer.getHost();
		this.user = ftpServer.getUsername();
		this.pwd = ftpServer.getPassword();
		this.port = ftpServer.getPort();
	}

	/*
	 * public FtpClient(FtpInfoWs ftpServer,int integer){
	 * 
	 * this.ftpHost = ftpServer.getHost(); this.user = ftpServer.getUsername();
	 * this.pwd = ftpServer.getPassword(); this.port = ftpServer.getPort(); }
	 */

	/**
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return client.isConnected();
	}

	/**
	 * 
	 * @throws FTPConnectionClosedException
	 */
	public void connect() throws FTPConnectionClosedException {
		disconnect();
		try {
			int portNumber = Integer.valueOf(port);
			client.connect(ftpHost, portNumber);
			int reply = client.getReplyCode();
			if (FTPReply.isPositiveCompletion(reply)) {
				client.login(user, pwd);
				reply = client.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					throw new FTPConnectionClosedException(client.getReplyString());
				}
			}
		} catch (Exception e) {
			throw new FTPConnectionClosedException(e.getClass().getCanonicalName() + ": " + e.getMessage());
		}
	}

	/**
	 * 
	 */
	public void disconnect() {
		try {
			client.logout();
		} catch (Exception ignored) {
		}
		try {
			client.disconnect();
		} catch (Exception ignored) {
		}
	}

	/**
	 * Upload filename with different target filename.
	 * 
	 * @param absoluteFilePath
	 * @param fileName
	 * @param directory
	 * @param targetFileName
	 * @param isBinary
	 * @return
	 */
	public boolean uploadFile(File file, String directory, String targetFileName, boolean isBinary) {
		boolean wasUploaded = false;

		try {
			if (isBinary) {
				client.setFileType(FTP.BINARY_FILE_TYPE);
			} else {
				client.setFileType(FTP.ASCII_FILE_TYPE);
			}
			if (directory != null && !directory.equals("")) {
				boolean isexists = client.changeWorkingDirectory(directory);
				if (!isexists) {
					createDirectoryTree(directory);
					// boolean test1 = client.makeDirectory(directory);
					boolean test = client.changeWorkingDirectory(directory);
					LOG.info("Change Directory : " + test + " Directory :" + directory);
				}
			}

			// File file = new File(absoluteFilePath + fileName);
			InputStream in = new FileInputStream(file);
			wasUploaded = client.storeFile(targetFileName, in);
			in.close();
			file = null;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return wasUploaded;
	}

	private void createDirectoryTree(String dirTree) throws IOException {

		boolean dirExists = true;
		int index = 0;
		// tokenize the string and attempt to change into each directory level. If you
		// cannot, then start creating.
		String[] directories = dirTree.split(AppConstants.SEPERATOR);
		for (String dir : directories) {
			if (!dir.isEmpty()) {
				if (dirExists) {
					dirExists = client.changeWorkingDirectory(dir);
				}
				if (!dirExists) {
					if (!client.makeDirectory(dir)) {
						throw new IOException("Unable to create remote directory '" + dir + "'.  error='"
								+ client.getReplyString() + "'");
					}
					if (!client.changeWorkingDirectory(dir)) {
						throw new IOException("Unable to change into newly created remote directory '" + dir
								+ "'.  error='" + client.getReplyString() + "'");
					}
				}
			} else if (index == 0) {
				client.changeWorkingDirectory(AppConstants.SEPERATOR);
				index = 1;
			}

		}
	}

}
