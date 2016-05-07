package ca.edmssubmit.backend.dirwatcher;

import java.io.File;

public interface FileCreateOrDeleteHandler {
	/**
	 * Called when a file is created. MUST NOT THROW.
	 * @param file File created.
	 */
	public void onFileCreate(File file);
	/**
	 * Called when a file is deleted. MUST NOT THROW.
	 * @param file File deleted.
	 */
	public void onFileDelete(File file);
}
