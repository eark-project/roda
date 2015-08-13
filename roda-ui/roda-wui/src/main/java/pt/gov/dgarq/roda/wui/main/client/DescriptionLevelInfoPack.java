package pt.gov.dgarq.roda.wui.main.client;

import java.io.Serializable;
import java.util.List;

import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevelInfo;

public class DescriptionLevelInfoPack implements Serializable {

	private static final long serialVersionUID = 3476633742750492225L;

	public List<DescriptionLevelInfo> descriptionLevelsInfo;
	public List<DescriptionLevel> descriptionLevels;
	public List<DescriptionLevel> rootDescriptionLevels;
	public List<DescriptionLevel> leafDescriptionLevels;
	public List<DescriptionLevel> representationDescriptionLevels;
	public List<DescriptionLevel> allButRepresentationDescriptionLevels;

	public DescriptionLevelInfoPack() {
	}

	public List<DescriptionLevelInfo> getDescriptionLevelsInfo() {
		return descriptionLevelsInfo;
	}

	public void setDescriptionLevelsInfo(
			List<DescriptionLevelInfo> descriptionLevelsInfo) {
		this.descriptionLevelsInfo = descriptionLevelsInfo;
	}

	public List<DescriptionLevel> getDescriptionLevels() {
		return descriptionLevels;
	}

	public void setDescriptionLevels(List<DescriptionLevel> descriptionLevels) {
		this.descriptionLevels = descriptionLevels;
	}

	public List<DescriptionLevel> getRootDescriptionLevels() {
		return rootDescriptionLevels;
	}

	public void setRootDescriptionLevels(
			List<DescriptionLevel> rootDescriptionLevels) {
		this.rootDescriptionLevels = rootDescriptionLevels;
	}

	public List<DescriptionLevel> getLeafDescriptionLevels() {
		return leafDescriptionLevels;
	}

	public void setLeafDescriptionLevels(
			List<DescriptionLevel> leafDescriptionLevels) {
		this.leafDescriptionLevels = leafDescriptionLevels;
	}

	public List<DescriptionLevel> getRepresentationDescriptionLevels() {
		return representationDescriptionLevels;
	}

	public void setRepresentationDescriptionLevels(
			List<DescriptionLevel> representationDescriptionLevels) {
		this.representationDescriptionLevels = representationDescriptionLevels;
	}

	public List<DescriptionLevel> getAllButRepresentationDescriptionLevels() {
		return allButRepresentationDescriptionLevels;
	}

	public void setAllButRepresentationDescriptionLevels(
			List<DescriptionLevel> allButRepresentationDescriptionLevels) {
		this.allButRepresentationDescriptionLevels = allButRepresentationDescriptionLevels;
	}

}
