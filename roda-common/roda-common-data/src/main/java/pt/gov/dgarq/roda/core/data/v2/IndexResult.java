package pt.gov.dgarq.roda.core.data.v2;

import java.io.Serializable;
import java.util.List;

public class IndexResult<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = -7896294396414765557L;

	private long offset;
	private long limit;
	private long totalCount;
	private List<T> results;
	private List<FacetFieldResult> facetResults;

	public IndexResult() {
		super();
	}

	public IndexResult(long offset, long limit, long totalCount, List<T> results, List<FacetFieldResult> facetResults) {
		super();
		this.offset = offset;
		this.limit = limit;
		this.totalCount = totalCount;
		this.results = results;
		this.setFacetResults(facetResults);
	}

	/**
	 * @return the offset
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * @return the limit
	 */
	public long getLimit() {
		return limit;
	}

	/**
	 * @return the totalCount
	 */
	public long getTotalCount() {
		return totalCount;
	}

	/**
	 * @return the results
	 */
	public List<T> getResults() {
		return results;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public List<FacetFieldResult> getFacetResults() {
		return facetResults;
	}

	public void setFacetResults(List<FacetFieldResult> facetResults) {
		this.facetResults = facetResults;
	}

	@Override
	public String toString() {
		return "IndexResult [offset=" + offset + ", limit=" + limit + ", totalCount=" + totalCount + ", results="
				+ results + ", facetResults=" + facetResults + "]";
	}
}
