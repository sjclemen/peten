package ca.edmssubmit.ui.viewmodel;

import com.google.common.base.Preconditions;

public class FileVM extends ObservableModel {
	private String name;
	private String directory;
	private String canonicalPath;
	private Integer order;
		
	public FileVM(String name, String directory, String canonicalPath, Integer order) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(directory);
		Preconditions.checkNotNull(canonicalPath);
		Preconditions.checkNotNull(order);
		this.name = name;
		this.directory = directory;
		this.canonicalPath = canonicalPath;
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		Preconditions.checkNotNull(name);
		firePropertyChange("name", this.name, this.name = name);
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		Preconditions.checkNotNull(directory);
		firePropertyChange("directory", this.directory, this.directory = directory);
	}
	
	public String getCanonicalPath() {
		return canonicalPath;
	}
	
	public void setCanonicalPath(String canonicalPath) {
		Preconditions.checkNotNull(canonicalPath);
		firePropertyChange("canonicalPath", this.canonicalPath, this.canonicalPath = canonicalPath);
	}
	
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		Preconditions.checkNotNull(order);
		firePropertyChange("order", this.order, this.order = order);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FileVM)) {
			return false;
		}
		FileVM other = (FileVM)obj;
		if (canonicalPath.equals(other.canonicalPath)) {
			return order.equals(other.getOrder());
		}
		return false;
	}
}
