/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.col.priceowl.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nz.co.tradeintel.listingsearch.ItemSample;
import nz.co.tradeintel.stats.ItemStats;
import nz.co.tradeintel.stats.ItemStatsImp;
import nz.co.tradeintel.trademe.util.ListingUtils;
import nz.co.trademe.api.v1.Item;
import nz.co.trademe.api.v1.ListedItemDetail;
import static org.apache.commons.math3.analysis.FunctionUtils.sample;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

/**
 *
 * @author Kevin
 */
public class AuctionStats {

    private ItemStats<Item> itemStats;
    private final int NUM_OF_RELEVANT_AUCTIONS_TO_SHOW = 5;
    private final int MIN_SOLD_FOR_ACCURACY = 5;
    private boolean isWeightedMeanCalculated = false;
    private double weightedMean;
    private List<Item> results;
    private ArrayList<Item> mostRelevantSoldAuctions = new ArrayList<>();
    private ArrayList<Item> mostRelevantUnsoldAuctions = new ArrayList<>();
    private final Accuracy accuracy;

    public AuctionStats(ItemSample<Item> results) {
        // We want a scaling of 1 for weighted stats to have the right array size.
        itemStats = new ItemStatsImp(results);
        this.results = results.getListings();
        for (Item lid : this.results) {
            if (ListingUtils.hasSold(lid)) {
                if (mostRelevantSoldAuctions.size()
                        == NUM_OF_RELEVANT_AUCTIONS_TO_SHOW) {
                    continue;
                }
                mostRelevantSoldAuctions.add(lid);
            } else {
                if (mostRelevantUnsoldAuctions.size()
                        == NUM_OF_RELEVANT_AUCTIONS_TO_SHOW) {
                    continue;
                }
                mostRelevantUnsoldAuctions.add(lid);
            }

            if (mostRelevantSoldAuctions.size()
                    == NUM_OF_RELEVANT_AUCTIONS_TO_SHOW
                    && mostRelevantUnsoldAuctions.size()
                    == NUM_OF_RELEVANT_AUCTIONS_TO_SHOW) {
                break;
            }
        }

        // Calculate accuracy.
        double coefficientOfVariance;
        if (itemStats.getMeanPrice() != 0) {
            coefficientOfVariance
                    = itemStats.getSoldPriceSD() / itemStats.getMeanPrice();
        } else {
            coefficientOfVariance = 0;
        }
        accuracy = Accuracy.fromCoefficientOfVar(coefficientOfVariance);
    }

    public List<Item> getMostRelevantSoldAuctions() {
        return mostRelevantSoldAuctions;
    }

    public List<Item> getMostRelevantUnsoldAuctions() {
        return mostRelevantUnsoldAuctions;
    }

    public ItemStats<Item> getItemStats() {
        return itemStats;
    }

    public Accuracy getAccuracy() {
        return accuracy;
    }
    
//    public double getWeightedMean() {
//        if(!isWeightedMeanCalculated) {
//            double[] values = new double[(int)itemStats.getSoldCount()+1];
//            double[] weightings = new double[(int) itemStats.getSoldCount()+1];
//            int i=0;
//            for(Map.Entry<ListedItemDetail, Float> r : results.entrySet()) {
//                if(ListingUtils.hasSold(r.getKey())) {
//                    values[i] = r.getKey().getMaxBidAmount().doubleValue();
//                    // Times the weightings by 3 to increase their effect.
//                    weightings[i] = r.getValue().doubleValue()*3;
//                }
//            }
//            Mean m = new Mean();
//            weightedMean = m.evaluate(values, weightings);
//        }
//        System.err.println("Normal Mean:" + itemStats.getMeanPrice());
//        return weightedMean;
//    }
    
    public boolean isSufficientData() {
        return itemStats.getSoldCount() >= MIN_SOLD_FOR_ACCURACY;
    }
}
