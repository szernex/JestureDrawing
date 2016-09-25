package org.szernex.java.jesturedrawing.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TextField;

public class FilteredTextField extends TextField {
	private SimpleStringProperty filterProperty = new SimpleStringProperty();

	public FilteredTextField() {

	}

	public FilteredTextField(String filter) {
		filterProperty.set(filter);
	}

	public String getFilterProperty() {
		return filterProperty.get();
	}

	public void setFilterProperty(String filterProperty) {
		this.filterProperty.set(filterProperty);
	}

	public SimpleStringProperty filterPropertyProperty() {
		return filterProperty;
	}

	@Override
	public void replaceText(int start, int end, String text) {
		if (validate(text))
			super.replaceText(start, end, text);
	}

	@Override
	public void replaceSelection(String replacement) {
		if (validate(replacement))
			super.replaceSelection(replacement);
	}


	private boolean validate(String value) {
		return value.matches(filterProperty.get());
	}
}
