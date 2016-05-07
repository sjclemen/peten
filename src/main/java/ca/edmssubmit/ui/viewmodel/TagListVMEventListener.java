package ca.edmssubmit.ui.viewmodel;

public interface TagListVMEventListener {
	public void onRename(TagVM tag, String oldName, String newName);
	public void onAdd(TagVM tag);
	public void onRemove(TagVM tag);
}
