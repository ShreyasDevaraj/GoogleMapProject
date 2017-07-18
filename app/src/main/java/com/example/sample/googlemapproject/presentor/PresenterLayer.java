package com.example.sample.googlemapproject.presentor;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import com.example.sample.googlemapproject.activity.MapActivity;
import com.example.sample.googlemapproject.cache.Cache;
import com.example.sample.googlemapproject.model.Crime;
import com.example.sample.googlemapproject.service.ChicagoCrimeNetworkService;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import static com.example.sample.googlemapproject.util.Constants.colorIndex;


public class PresenterLayer implements PresenterInteractor {

    private static final String TAG = PresenterLayer.class.getSimpleName();
    private final MapActivity view;
    private final ChicagoCrimeNetworkService service;
    private Disposable currentRequest;

    public PresenterLayer(final MapActivity view, final ChicagoCrimeNetworkService service) {
        Log.d(TAG, "Presenter layer created");
        this.view = view;
        this.service = service;
    }

    @Override
    public void makePageRequest(final int pageIndex) {
        final Date today = new Date();
        final DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        final Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -30);
        final Date today30 = cal.getTime();
        unSubscribe();
        final Observable<Crime[]> crimeListObservable = (Observable<Crime[]>) service.getPreparedObservable(service.getAPI().getMajorCrimeList(dateFormat.format(today30), "50", String.valueOf(pageIndex)), Crime.class, true, false);

        final Scheduler scheduler = Schedulers.newThread();
        crimeListObservable.subscribeOn(scheduler).observeOn(scheduler).subscribe(new Observer<Crime[]>() {
            @Override
            public void onSubscribe(final Disposable d) {
                Log.d(TAG, "Subscribed for page request");
                currentRequest = d;
            }

            @Override
            public void onNext(final Crime[] value) {
                if(value.length == 0) {
                    Log.d(TAG, "No results found ");
                    final Handler handler = new Handler(view.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(view.getApplicationContext(), "No more results to load.. ", Toast.LENGTH_LONG).show();
                        }
                    });
                    view.onRequestComplete(new ArrayList<Integer>(), new ArrayList<Crime>());
                }
                else {
                    final ArrayList<Crime> crimeData = new ArrayList<>(Arrays.asList(value));
                    final Map<Integer, Integer> map = new HashMap<>();
                    for(final Crime crime : value) {
                        final int key = Integer.parseInt(crime.getCpdDistrict());
                        if(map.containsKey(key)) {
                            map.put(key, map.get(key) + 1);
                        }
                        else {
                            map.put(key, 1);
                        }
                    }
                    final ArrayList<Integer> indexes = sortByComparator(map);
                    view.onRequestComplete(indexes, crimeData);
                    Log.d(TAG, "Page request complete with " + value.length + " results");
                }


            }

            @Override
            public void onError(final Throwable e) {
                view.onErrorResult(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void makeSearchRequest(final String query, final int pageIndex) {
        final Date today = new Date();
        final DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        final Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -30);
        final Date today30 = cal.getTime();
        unSubscribe();
        final Observable<Crime[]> crimeByTypeObservable = (Observable<Crime[]>) service.getPreparedObservable(service.getAPI().getCrimesByType(query, dateFormat.format(today30), "50", String.valueOf(pageIndex)), Crime.class, true, false);

        final Scheduler scheduler = Schedulers.newThread();
        crimeByTypeObservable.subscribeOn(scheduler).observeOn(scheduler).subscribe(new Observer<Crime[]>() {
            @Override
            public void onSubscribe(final Disposable d) {
                currentRequest = d;
                Log.d(TAG, "Subscribed search request");
            }

            @Override
            public void onNext(final Crime[] value) {
                if(value.length == 0) {
                    Log.d(TAG, "No results found ");
                    view.onRequestComplete(new ArrayList<Integer>(), new ArrayList<Crime>());
                    final Handler handler = new Handler(view.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(view.getApplicationContext(), "No more results to load.. ", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    final ArrayList<Crime> crimeData = new ArrayList<>(Arrays.asList(value));
                    final Map<Integer, Integer> map = new HashMap<>();
                    for(final Crime crime : value) {
                        final int key = Integer.parseInt(crime.getCpdDistrict());
                        if(map.containsKey(key)) {
                            map.put(key, map.get(key) + 1);
                        }
                        else {
                            map.put(key, 1);
                        }
                    }

                    final ArrayList<Integer> indexes = sortByComparator(map);
                    for(final Crime crime : value) {
                        final String color = colorIndex.get(indexes.indexOf(Integer.parseInt(crime.getCpdDistrict()))) == null ? "#000000" : colorIndex.get(indexes.indexOf(Integer.parseInt(crime.getCpdDistrict())));
                        crime.setColorCode(color);
                    }
                    view.onRequestComplete(indexes, crimeData);
                }
                Cache.getInstance().put(query + String.valueOf(pageIndex), value);
                Log.d(TAG, "Search result success " + value.length);
            }

            @Override
            public void onError(final Throwable e) {
                view.onErrorResult(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void unSubscribe() {
        if(currentRequest != null && !currentRequest.isDisposed()) {
            Log.d(TAG, "Un-subscribing due to life cycle of activity or second successive request fired ");
            currentRequest.dispose();
            currentRequest = null;
        }
    }

    /**
     * Sorts the crime data with respect to frequency of crimes in each city
     *
     * @param unsortedMap un-sorted crime
     * @return List of sorted crime
     */
    private ArrayList<Integer> sortByComparator(final Map<Integer, Integer> unsortedMap) {
        final ArrayList<Integer> list = new ArrayList<>(unsortedMap.keySet());
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(final Integer x, final Integer y) {
                return unsortedMap.get(y) - unsortedMap.get(x);
            }
        });
        return list;
    }
}
