package pt.gov.dgarq.roda.core.data.v2;

import java.io.Serializable;

public class FacetValue implements Serializable {
	private static final long serialVersionUID = 8898599554012120196L;

	private String value;
	private long count;

	public FacetValue() {
		super();
	}

	public FacetValue(String value, long count) {
		super();
		this.value = value;
		this.count = count;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "FacetValue [value=" + value + ", count=" + count + "]";
	}

}