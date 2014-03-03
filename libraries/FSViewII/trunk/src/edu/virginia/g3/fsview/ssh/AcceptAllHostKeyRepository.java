package edu.virginia.g3.fsview.ssh;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.UserInfo;

final class AcceptAllHostKeyRepository implements HostKeyRepository {
	@Override
	public void add(HostKey arg0, UserInfo arg1) {
	}

	@Override
	public int check(String arg0, byte[] arg1) {
		return 0;
	}

	@Override
	public HostKey[] getHostKey() {
		return null;
	}

	@Override
	public HostKey[] getHostKey(String arg0, String arg1) {
		return null;
	}

	@Override
	public String getKnownHostsRepositoryID() {
		return null;
	}

	@Override
	public void remove(String arg0, String arg1) {
	}

	@Override
	public void remove(String arg0, String arg1, byte[] arg2) {
	}
}