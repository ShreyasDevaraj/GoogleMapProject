package com.example.sample.googlemapproject.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.example.sample.googlemapproject.R;
import com.example.sample.googlemapproject.application.MapApplication;
import com.example.sample.googlemapproject.cache.Cache;
import com.example.sample.googlemapproject.model.Crime;
import com.example.sample.googlemapproject.presentor.PresenterInteractor;
import com.example.sample.googlemapproject.presentor.PresenterLayer;
import com.example.sample.googlemapproject.service.ChicagoCrimeNetworkService;
import com.example.sample.googlemapproject.util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.jhlabs.map.proj.ProjectionUtils;
import com.jhlabs.map.proj.StateProjection;
import static android.util.Log.d;
import static android.util.Log.e;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.sample.googlemapproject.R.array.crimes;

/**
 * The main map activity of the application
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapActivity.class.getSimpleName();
    private GoogleMap myGoogleMap;
    private final List<Marker> markerList = new ArrayList<>();
    private static int currentPageIndex = 0;
    private Button nextPage;
    private Button home;
    private PresenterInteractor presenter;
    private ArrayList<Crime> crimeList;
    private ArrayList<Integer> indexes;
    private ProgressBar progressBar;
    private boolean isLayerBoundaryLoaded = false;
    private String currentSearchString;
    private int check = 0;
    private int currentSearchPosition = 0;
    private static final String CRIME_LIST_KEY = "CrimeList";
    private static final String INDEX_LIST_KEY = "IndexList";
    private static final String BOUNDARY_KEY = "Boundary";
    private static final String PAGE_INDEX_KEY = "PageIndex";
    private static final String CURRENT_SEARCH_WORD_KEY = "CurrentSearchKey";
    private static final String CURRENT_SEARCH_POSITION_KEY = "CurrentSearchPosition";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_id);
        final ChicagoCrimeNetworkService networkService = MapApplication.getInstance().getChicagoCrimeNetworkService();
        presenter = new PresenterLayer(this, networkService);
        nextPage = (Button) findViewById(R.id.nextPage);
        home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new HomePageClickListener());
        nextPage.setOnClickListener(new NextPageClickListener());
        progressBar = (ProgressBar) findViewById(R.id.loadingPanel);
        if(savedInstanceState != null) {
            d(TAG, "previous instance saved.");
            crimeList = savedInstanceState.getParcelableArrayList(CRIME_LIST_KEY);
            indexes = savedInstanceState.getIntegerArrayList(INDEX_LIST_KEY);
            isLayerBoundaryLoaded = savedInstanceState.getBoolean(BOUNDARY_KEY);
            currentPageIndex = savedInstanceState.getInt(PAGE_INDEX_KEY);
            currentSearchString = savedInstanceState.getString(CURRENT_SEARCH_WORD_KEY);
            currentSearchPosition = savedInstanceState.getInt(CURRENT_SEARCH_POSITION_KEY);

        }
        else {
            mapFragment.setRetainInstance(true);
            presenter.makePageRequest(0);
            progressBar.setVisibility(VISIBLE);
        }
        if(currentPageIndex != 0) {
            home.setVisibility(VISIBLE);
            home.setText(MapApplication.getInstance().getString(R.string.home_button_text));
            nextPage.setText(MapApplication.getInstance().getString(R.string.next_button_text, String.valueOf((currentPageIndex / 50) + 1)));
        }
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if map object is null
     */
    private void setUpMapIfNeeded() {
        if(myGoogleMap == null) {
            d(TAG, "Loading map");
            final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_id);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        d(TAG, "OnPause");
        presenter.unSubscribe();
        progressBar.setVisibility(GONE);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        if(crimeList != null && indexes != null) {
            d(TAG, "onSaveInstanceState: Saving state of UI");
            outState.putParcelableArrayList(CRIME_LIST_KEY, crimeList);
            outState.putIntegerArrayList(INDEX_LIST_KEY, indexes);
            outState.putBoolean(BOUNDARY_KEY, true);
            outState.putInt(PAGE_INDEX_KEY, currentPageIndex);
            outState.putString(CURRENT_SEARCH_WORD_KEY, currentSearchString);
            outState.putInt(CURRENT_SEARCH_POSITION_KEY, currentSearchPosition);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        d(TAG, "OnMapReady");
        myGoogleMap = googleMap;
        final LatLng chicago = new LatLng(41.864, -87.684);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(chicago));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));
        if(indexes != null && crimeList != null) {
            loadMarker(indexes, crimeList);
        }
        if(!isLayerBoundaryLoaded) {
            d(TAG, "drawing boundary");
            final HandlerThread thread = new HandlerThread("HandlerThread");
            thread.start();
            final Handler loadBoundaryHandler = new Handler(thread.getLooper());
            loadBoundaryHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        final GeoJsonLayer layer = new GeoJsonLayer(googleMap, R.raw.cityboundary, getApplicationContext());
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                layer.addLayerToMap();
                                layer.getDefaultPolygonStyle().setStrokeWidth(3.0f);
                                layer.getDefaultPolygonStyle().setFillColor(Color.parseColor("#96d3d3d3"));
                            }
                        });

                    }
                    catch(final IOException e) {
                        Log.e(TAG, "IO exception while drawing boundary " + e);
                    }
                    catch(final JSONException e) {
                        Log.e(TAG, "JSON exception while drawing boundary " + e);
                    }
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        d(TAG, "onCreateOptionsMenu");
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        final MenuItem item = menu.findItem(R.id.spinner);
        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, crimes, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerClickListener());
        spinner.setSelection(currentSearchPosition);
        return true;
    }

    /**
     * Launches a search query if not found in cache. If found will directly retrieve from cache
     *
     * @param query query given by user
     * @param pageIndex current page index
     */
    private void launchSearchQuery(final String query, final int pageIndex) {
        final Cache<String, Crime[]> instance = Cache.getInstance();
        if(instance.get(query + String.valueOf(pageIndex)) == null) {
            d(TAG, "Fetching search result from server with query : " + query + " and page index " + pageIndex);
            presenter.makeSearchRequest(query, pageIndex);
            progressBar.setVisibility(VISIBLE);
            removeMarkers();
        }
        else {
            d(TAG, "Fetching search result from cache for query :" + query + " and page index " + pageIndex);
            updateMap(instance.get(query + String.valueOf(pageIndex)));
            progressBar.setVisibility(GONE);
        }
    }

    /**
     * Updates the map if search query is found in cache
     *
     * @param data crime list found in cache
     */
    private void updateMap(final Crime[] data) {
        if(myGoogleMap != null) {
            removeMarkers();
            for(final Crime crime : data) {
                if(crime.getYCoordinate() != null && crime.getXCoordinate() != null) {
                    final LatLng latLong = ProjectionUtils.getLatLng(this, StateProjection.ILLINOIS_STATE_PLANE, crime.getXCoordinate(), crime.getYCoordinate());
                    markerList.add(myGoogleMap.addMarker(new MarkerOptions().position(latLong).title(crime.getPrimary()).icon(getMarkerIcon(crime.getColorCode()))));
                }
            }
            d(TAG, "markers loaded after load from cache");
        }
    }

    /**
     * Loads the map with the markers with different colors based on the crime frequency in every district.
     *
     * @param indexes order of crime with respect to frequency of occurence
     * @param crimeData list of crime data
     */
    private void loadMarker(final List<Integer> indexes, final ArrayList<Crime> crimeData) {
        if(myGoogleMap != null) {
            for(final Crime crime : crimeData) {
                if(crime.getXCoordinate() != null && crime.getYCoordinate() != null) {
                    final LatLng latLong = ProjectionUtils.getLatLng(this, StateProjection.ILLINOIS_STATE_PLANE, crime.getXCoordinate(), crime.getYCoordinate());
                    final String color = Constants.colorIndex.get(indexes.indexOf(Integer.parseInt(crime.getCpdDistrict()))) == null ? "#000000" : Constants.colorIndex.get(indexes.indexOf(Integer.parseInt(crime.getCpdDistrict())));
                    markerList.add(myGoogleMap.addMarker(new MarkerOptions().position(latLong).title(crime.getCpdDistrict()).icon(getMarkerIcon(color))));
                }
            }
            progressBar.setVisibility(GONE);
            d(TAG, "markers loaded");
        }
    }

    /**
     * creates a bitmap of the given color
     *
     * @param color of the required bitmap
     * @return bitmap Descriptor of the given color.
     */
    private BitmapDescriptor getMarkerIcon(final String color) {
        final Bitmap image = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        image.eraseColor(Color.parseColor(color));
        return BitmapDescriptorFactory.fromBitmap(getCircleBitmap(image, Color.parseColor(color)));
    }

    /**
     * creates a circular bitmap
     *
     * @param bitmap bitmap to be converted into a circle
     * @param color color of the bitmap
     * @return converted bitmap
     */
    private Bitmap getCircleBitmap(final Bitmap bitmap, final int color) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    /**
     * Gets triggered when requested api GET request is finished
     *
     * @param indexes order of crimes with respect to frequency
     * @param crimeList list of crimes
     */
    public void onRequestComplete(final ArrayList<Integer> indexes, final ArrayList<Crime> crimeList) {
        d(TAG, "Request complete with " + crimeList.size() + "results");
        this.crimeList = crimeList;
        this.indexes = indexes;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadMarker(indexes, crimeList);
            }
        });

    }

    /**
     * Called when there is an error in web service request
     *
     * @param e error thrown
     */
    public void onErrorResult(final Throwable e) {
        e(TAG, "Error in loading result " + e);
        Toast.makeText(this, "Loading failed.. Please try again ", Toast.LENGTH_SHORT).show();
    }

    /**
     * Removes all markers on the map
     */
    private void removeMarkers() {
        d(TAG, "removing markers " + markerList.size());
        if(!markerList.isEmpty()) {
            for(final Marker marker : markerList) {
                marker.remove();
            }
            markerList.clear();
        }
    }

    /**
     * next page button click listener
     */
    class NextPageClickListener implements View.OnClickListener {

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(final View v) {
            d(TAG, "next button clicked");
            home.setVisibility(VISIBLE);
            home.setText(MapApplication.getInstance().getString(R.string.home_button_text));
            currentPageIndex += 50;
            d(TAG, "current offset is " + currentPageIndex);
            if(currentSearchString.equals(getResources().getStringArray(R.array.crimes)[0])) {
                d(TAG, "making request for major crimes");
                presenter.makePageRequest(currentPageIndex);
                progressBar.setVisibility(VISIBLE);
                removeMarkers();
            }
            else {
                d(TAG, "making request for selected type ");
                launchSearchQuery(currentSearchString, currentPageIndex);
            }
            nextPage.setText(MapApplication.getInstance().getString(R.string.next_button_text, String.valueOf((currentPageIndex / 50) + 1)));

        }
    }

    /**
     * home button click listener
     */
    class HomePageClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            d(TAG, "home button clicked");
            currentPageIndex = 0;
            nextPage.setText(MapApplication.getInstance().getString(R.string.next_button_text, String.valueOf((currentPageIndex / 50) + 1)));
            if(currentSearchString.equals(getResources().getStringArray(R.array.crimes)[0])) {
                d(TAG, "making request for home page for major crimes");
                presenter.makePageRequest(currentPageIndex);
                home.setVisibility(GONE);
                progressBar.setVisibility(VISIBLE);
                removeMarkers();
            }
            else {
                d(TAG, "making request for home page for selected crime type crimes");
                home.setVisibility(GONE);
                launchSearchQuery(currentSearchString, currentPageIndex);
            }

        }
    }

    /**
     * Search spinner click listener
     */
    class SpinnerClickListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
            currentSearchString = parent.getAdapter().getItem(position).toString();
            // Doing this check because this listener gets triggered as soon as the activity loads.
            if(++check > 1) {
                currentSearchPosition = position;
                d(TAG, "current search key " + currentSearchString);
                currentPageIndex = 0;
                nextPage.setText(MapApplication.getInstance().getString(R.string.next_button_text, String.valueOf((currentPageIndex / 50) + 1)));
                home.setVisibility(GONE);
                progressBar.setVisibility(VISIBLE);
                removeMarkers();
                if(currentSearchString.equals(getResources().getStringArray(R.array.crimes)[0])) {
                    d(TAG, "user selected major crimes. fetching it");
                    presenter.makePageRequest(0);
                }
                else {
                    d(TAG, "user selected a type of crimes . fetching it");
                    launchSearchQuery(currentSearchString, 0);
                }
            }

        }

        @Override
        public void onNothingSelected(final AdapterView<?> parent) {
            Log.d(TAG, "Nothing selected");
        }
    }
}
