package ca.edmssubmit.ui;

import org.eclipse.core.databinding.observable.Realm;

public class RealmHelper extends Realm {

	public RealmHelper(Realm r) {
		setDefault(r);
	}
	
	@Override
	public boolean isCurrent() {
		return true;
	}

}
