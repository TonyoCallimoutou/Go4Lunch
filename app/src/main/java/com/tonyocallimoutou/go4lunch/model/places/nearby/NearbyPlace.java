package com.tonyocallimoutou.go4lunch.model.places.nearby;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tonyocallimoutou.go4lunch.model.places.RestaurantDetails;

import java.util.ArrayList;
import java.util.List;

public class NearbyPlace {

    @SerializedName("html_attributions")
    @Expose
    private List<Object> htmlAttributions = new ArrayList<Object>();
    @SerializedName("next_page_token")
    @Expose
    private String nextPageToken;
    @SerializedName("results")
    @Expose
    private List<RestaurantDetails> placeResults = new ArrayList<RestaurantDetails>();
    @SerializedName("status")
    @Expose
    private String status;

    /**
     *
     * @return
     * The htmlAttributions
     */
    public List<Object> getHtmlAttributions() {
        return htmlAttributions;
    }

    /**
     *
     * @param htmlAttributions
     * The html_attributions
     */
    public void setHtmlAttributions(List<Object> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    /**
     *
     * @return
     * The nextPageToken
     */
    public String getNextPageToken() {
        return nextPageToken;
    }

    /**
     *
     * @param nextPageToken
     * The next_page_token
     */
    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    /**
     *
     * @return
     * The results
     */
    public List<RestaurantDetails> getResults() {
        return placeResults;
    }

    /**
     *
     * @param placeResults
     * The results
     */
    public void setResults(List<RestaurantDetails> placeResults) {
        this.placeResults = placeResults;
    }

    /**
     *
     * @return
     * The status
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     * @param status
     * The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
