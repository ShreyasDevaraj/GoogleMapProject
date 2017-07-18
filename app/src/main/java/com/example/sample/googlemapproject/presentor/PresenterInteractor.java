package com.example.sample.googlemapproject.presentor;


public interface PresenterInteractor {

    /**
     * Makes paged request for type major crime
     *
     * @param pageIndex page index of request
     */
    void makePageRequest(final int pageIndex);

    /**
     * Makes search request for passed in query and page index
     *
     * @param query to be searched
     * @param pageIndex page index
     */
    void makeSearchRequest(final String query, final int pageIndex);

    /**
     * Un-subscribes the observer
     */
    void unSubscribe();
}
