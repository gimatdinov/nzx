package ru.otr.nzx.ftp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;

public class FTPUserManager implements org.apache.ftpserver.ftplet.UserManager {

	private final String ftpDirectory;
	private final boolean anonymousEnable;
	private final FTPUser anonymous;
	private final Map<String, FTPUser> notStoredUsers;

	private final String adminName = "admin";

	public FTPUserManager(String ftpDirectory, boolean anonymousEnable) {
		if (ftpDirectory == null) {
			throw new IllegalArgumentException();
		}
		this.ftpDirectory = ftpDirectory;
		this.anonymousEnable = anonymousEnable;
		this.anonymous = new FTPUser("anonymous", null, ftpDirectory, ".", false);
		this.notStoredUsers = new HashMap<>();
		this.notStoredUsers.put(anonymous.getName(), anonymous);
	}

	@Override
	public User authenticate(Authentication _auth) throws AuthenticationFailedException {
		if (anonymousEnable) {
			return anonymous;
		}
		if (_auth instanceof UsernamePasswordAuthentication) {
			UsernamePasswordAuthentication auth = (UsernamePasswordAuthentication) _auth;
			FTPUser user = notStoredUsers.get(auth.getUsername().toLowerCase());
			if (!user.getPassword().equals(auth.getPassword())) {
				user = null;
			}
			return user;
		}
		return null;
	}

	@Override
	public void delete(String userName) throws FtpException {
		FTPUser user = notStoredUsers.remove(userName.toLowerCase());
		if (user != null) {
			new File(ftpDirectory, user.getFolder()).delete();
		}
	}

	@Override
	public boolean doesExist(String userName) throws FtpException {
		FTPUser user = notStoredUsers.get(userName.toLowerCase());
		return (user != null);
	}

	@Override
	public String getAdminName() throws FtpException {
		return adminName;
	}

	@Override
	public String[] getAllUserNames() throws FtpException {
		return notStoredUsers.keySet().toArray(new String[] {});
	}

	@Override
	public User getUserByName(String userName) throws FtpException {
		FTPUser user = notStoredUsers.get(userName.toLowerCase());

		if (user != null) {
			prepareUserFolder(user);
		}
		return user;
	}

	@Override
	public boolean isAdmin(String userName) throws FtpException {
		return false;
	}

	@Override
	public void save(User user) throws FtpException {
		if (user instanceof FTPUser) {
			FTPUser ftpUser = (FTPUser) user;
			prepareUserFolder(ftpUser);
			notStoredUsers.put(ftpUser.getName(), ftpUser);
		} else {
			throw new FtpException("Unsupported class: " + user.getClass().getName());
		}
	}

	private void prepareUserFolder(FTPUser user) {
		File dir = new File(ftpDirectory, user.getFolder());
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public String getFtpDirectory() {
		return ftpDirectory;
	}

}
