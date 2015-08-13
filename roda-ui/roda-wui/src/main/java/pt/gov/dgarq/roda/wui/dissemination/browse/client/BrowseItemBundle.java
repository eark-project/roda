package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.io.Serializable;
import java.util.List;

import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;

public class BrowseItemBundle implements Serializable {

	private static final long serialVersionUID = 7901536603462531124L;

	private SimpleDescriptionObject sdo;
	private List<SimpleDescriptionObject> sdoAncestors;
	private List<DescriptiveMetadataBundle> descriptiveMetadata;
	private List<Representation> representations;

	public BrowseItemBundle() {
		super();

	}

	public BrowseItemBundle(SimpleDescriptionObject sdo, List<SimpleDescriptionObject> sdoAncestors,
			List<DescriptiveMetadataBundle> descriptiveMetadata, List<Representation> representations) {
		super();
		this.sdo = sdo;
		this.setSdoAncestors(sdoAncestors);
		this.descriptiveMetadata = descriptiveMetadata;
		this.representations = representations;
	}

	public SimpleDescriptionObject getSdo() {
		return sdo;
	}

	public void setSdo(SimpleDescriptionObject sdo) {
		this.sdo = sdo;
	}

	public List<DescriptiveMetadataBundle> getDescriptiveMetadata() {
		return descriptiveMetadata;
	}

	public void setDescriptiveMetadata(List<DescriptiveMetadataBundle> descriptiveMetadata) {
		this.descriptiveMetadata = descriptiveMetadata;
	}

	public List<Representation> getRepresentations() {
		return representations;
	}

	public void setRepresentations(List<Representation> representations) {
		this.representations = representations;
	}

	public List<SimpleDescriptionObject> getSdoAncestors() {
		return sdoAncestors;
	}

	public void setSdoAncestors(List<SimpleDescriptionObject> sdoAncestors) {
		this.sdoAncestors = sdoAncestors;
	}

}
