package editor.version;

public class Version {
	public static int v_major = 1, v_minor = 3, v_revision = 515;

	String versionString;
	String changesString;

	int major, minor, revision;

	public Version(String version, String changes) {
		this.versionString = version;
		this.changesString = changes;

		changesString = changesString.replace("-", "\n\t-");

		// Split up the line.
		String[] lineSplit = versionString.split("\\.");

		// Get ints for the version numbers.
		major = Integer.parseInt(lineSplit[0]);
		minor = Integer.parseInt(lineSplit[1]);
		revision = Integer.parseInt(lineSplit[2]);
	}

	public int compare() {
		if (major > v_major)
			return -1;
		else if (major == v_major) {
			if (minor > v_minor)
				return -2;
			else if (major == v_minor) {
				if (revision > v_revision)
					return -3;
			}
		}

		return 1;
	}
}