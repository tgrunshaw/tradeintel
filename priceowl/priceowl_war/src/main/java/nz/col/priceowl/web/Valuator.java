/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nz.col.priceowl.web;

import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import nz.co.tradeintel.listingsearch.ItemSample;
import nz.co.tradeintel.listingsearch.ListingSearch;
import nz.co.tradeintel.listingsearch.ListingSearcher;
import nz.co.tradeintel.trademe.Category;

/**
 *
 * @author Kevin
 */
@Named
@RequestScoped
public class Valuator {
	@EJB(beanName = "SolrSearcher")
	private ListingSearcher searcher;
	private boolean isSearchComplete = false;
	private ListingSearch search;
	private Map<Integer, Category> suggestedCategories;
	private AuctionStats stats;

	private void setSearchTerms(String searchTerms) {
		search.setSearchTerms(searchTerms);
	}
	public void setNewFilter() {
		search.setCondition(ListingSearch.Condition.NEW);
		execute();
	}

	public void setUsedFilter() {
		search.setCondition(ListingSearch.Condition.USED);
		execute();
	}

	public void setAnyFilter() {
		search.setCondition(ListingSearch.Condition.ANY);
		execute();
	}

	public void setCategory(int suggestedCategoryIndex) {
		search.setCategory(suggestedCategories.get(suggestedCategoryIndex));
		execute();
	}

	public String getSuggestedCategory(int index) {
		return suggestedCategories.get(index).getName();
	}
	
	public void execute() {
		ItemSample results = searcher.getItemSample(search);
		stats = new AuctionStats(results);
	}

	public AuctionStats getStats() {
		return stats;
	}
	
	
}
