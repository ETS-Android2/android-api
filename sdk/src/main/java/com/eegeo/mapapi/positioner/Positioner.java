package com.eegeo.mapapi.positioner;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.eegeo.mapapi.geometry.ElevationMode;
import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.util.NativeApiObject;

import java.util.concurrent.Callable;
/**
 * A Positioner represents a single point on the map. The primary purpose of a positioner is to
 * expose a point on the map as a 2D coordinate in screen space. This could be used for example to
 * position a View.
 * <br>
 * <br>
 * To create a Positioner and add it to the map, use EegeoMap.addPositioner()
 * <br>
 * <br>
 * Positioner has the following properties:
 * <br>
 * <br>
 * <b>Position</b><br>
 * The LatLng location of the positioner on the map. The position can be changed at any point if you
 * want to move the positioner.
 * <br>
 * <br>
 * <b>Elevation</b><br>
 * The height above the map at which the positioner is located, in meters. Elevation can be
 * specified as either a height above terrain, or as an absolute altitude - see ElevationMode below.
 * <br>
 * <br>
 * <b>ElevationMode</b><br>
 * Specifies how the Elevation property is interpreted, as either:
 * <br>
 * A height above the terrain (ground), in meters.
 * <br>
 * An absolute altitude (height above mean sea level), in meters.
 * <br>
 * <br>
 * <b>IndoorMapId</b><br>
 * Positioners can be displayed on indoor maps. This property stores the string identifier of the
 * indoor map on which the positioner is to be located. For outdoor positioners, this property is
 * empty.
 * <br>
 * <br>
 * <b>IndoorFloorId</b><br>
 * For a positioner displayed on an indoor map, the identifier of the floor on which the positioner
 * is to be displayed.
 * <br>
 * <br>
 * <b>PositionerChangedListener</b><br>
 * Each time the screen space coordinate of the positioner changes the PositionerChangedListener
 * will be called.
 * <br>
 * <br>
 * Public methods in this class must be called on the Android UI thread.
 */
public class Positioner extends NativeApiObject {

    private static final AllowHandleAccess m_allowHandleAccess = new AllowHandleAccess();
    private final PositionerApi m_positionerApi;
    private String m_indoorMapId;
    private int m_indoorFloorId;
    private LatLng m_position;
    private double m_elevation;
    private ElevationMode m_elevationMode;
    private OnPositionerChangedListener m_positionerChangedListener;
    private Point m_screenPoint = new Point();
    private boolean m_isScreenPointValid = false;
    private boolean m_isBehindGlobeHorizon = false;

    /**
     * This constructor is for internal SDK use only -- use EegeoMap.addPositioner to create a positioner
     *
     * @eegeo.internal
     */
    @UiThread
    public Positioner(@NonNull final PositionerApi positionerApi,
                  @NonNull final PositionerOptions positionerOptions) {
        super(positionerApi.getNativeRunner(), positionerApi.getUiRunner(),
                new Callable<Integer>() {
                    @WorkerThread
                    @Override
                    public Integer call() throws Exception {
                        return positionerApi.createPositioner(positionerOptions, m_allowHandleAccess);
                    }
                });

        m_positionerApi = positionerApi;
        m_position = positionerOptions.getPosition();
        m_elevation = positionerOptions.getElevation();
        m_elevationMode = positionerOptions.getElevationMode();
        m_indoorMapId = positionerOptions.getIndoorMapId();
        m_indoorFloorId = positionerOptions.getIndoorFloorId();
        m_positionerChangedListener = positionerOptions.getPositionerChangedListener();

        submit(new Runnable() {
            @WorkerThread
            @Override
            public void run() {
                positionerApi.registerPositioner(Positioner.this, m_allowHandleAccess);
            }
        });
    }

    /**
     * Returns the position of the positioner.
     *
     * @return A LatLng object representing the location of the positioner on the map's surface.
     */
    @UiThread
    public LatLng getPosition() {
        return m_position;
    }

    /**
     * Sets the location of this positioner.
     *
     * @param position A LatLng coordinate.
     */
    @UiThread
    public void setPosition(@NonNull LatLng position) {
        m_position = position;
        updateLocation();
    }

    /**
     * Returns the current elevation of the positioner. The property is interpreted differently,
     * depending on the ElevationMode property.
     *
     * @return A height, in meters.
     */
    @UiThread
    public double getElevation() {
        return m_elevation;
    }

    /**
     * Sets the elevation of this positioner
     *
     * @param elevation A height in meters. Interpretation depends on the current
     *                  PositionerOptions.PositionerElevationMode
     */
    @UiThread
    public void setElevation(double elevation) {
        m_elevation = elevation;
        updateLocation();
    }

    /**
     * Returns the mode specifying how the Elevation property is interpreted.
     *
     * @return An enumerated value indicating whether Elevation is specified as a height above
     * terrain, or an absolute altitude above sea level.
     */
    @UiThread
    public ElevationMode getElevationMode() {
        return m_elevationMode;
    }

    /**
     * Sets the elevation mode for this positioner
     *
     * @param elevationMode The mode specifying how to interpret the Elevation property
     */
    @UiThread
    public void setElevationMode(ElevationMode elevationMode) {
        m_elevationMode = elevationMode;
        updateLocation();
    }

    /**
     * Gets the identifier of an indoor map on which this positioner should be displayed, if any.
     *
     * @return For a positioner on an indoor map, the string identifier of the indoor map; otherwise an
     * empty string.
     */
    @UiThread
    public String getIndoorMapId() {
        return m_indoorMapId;
    }

    /**
     * Sets the indoor map ID.
     *
     * @param indoorMapId The map ID.
     */
    @UiThread
    public void setIndoorMapId(String indoorMapId) {
        m_indoorMapId = indoorMapId;
        updateLocation();
    }

    /**
     * Gets the identifier of an indoor map floor on which this positioner should be displayed, if any.
     *
     * @return For a positioner on an indoor map, the identifier of the floor; otherwise 0.
     */
    @UiThread
    public int getIndoorFloorId() {
        return m_indoorFloorId;
    }

    /**
     * Sets the indoor floor ID.
     *
     * @param indoorFloorId The floor ID.
     */
    @UiThread
    public void setIndoorFloorId(int indoorFloorId) {
        m_indoorFloorId = indoorFloorId;
        updateLocation();
    }

    /**
     * Returns the screen point if available or null. For a Positioner placed on an indoor map
     * floor, returns null unless that floor is the current focus of the indoor map.
     *
     * @return Returns the screen point if available or null.
     */
    @UiThread
    public Point tryGetScreenPoint() {
        return m_isScreenPointValid?m_screenPoint:null;
    }

    /**
     * Returns true if the positioner is over the horizon. When the positioner is on the far side of
     * the world it may still be within the screen area but should not be visible. In those
     * situations this can be used to hide views.
     *
     * @return True if the positioner is beyond the horizon.
     */
    @UiThread
    public boolean isBehindGlobeHorizon() { return m_isBehindGlobeHorizon; }

    /**
     * Removes this positioner from the map and destroys the positioner. Use EegeoMap.removePositioner
     *
     * @eegeo.internal
     */
    @UiThread
    public void destroy() {
        submit(new Runnable() {
            @WorkerThread
            @Override
            public void run() {
                m_positionerApi.destroy(Positioner.this, Positioner.m_allowHandleAccess);
                m_positionerApi.unregisterPositioner(Positioner.this, Positioner.m_allowHandleAccess);
            }
        });

    }

    @UiThread
    private void updateLocation() {
        final LatLng position = m_position;
        final double elevation = m_elevation;
        final ElevationMode elevationMode = m_elevationMode;
        final String indoorMapId = m_indoorMapId;
        final int indoorFloorId = m_indoorFloorId;

        submit(new Runnable() {
            @WorkerThread
            public void run() {
                m_positionerApi.updateLocation(getNativeHandle(), Positioner.m_allowHandleAccess, position, elevation, elevationMode, indoorMapId, indoorFloorId);
            }
        });
    }

    @WorkerThread
    int getNativeHandle(AllowHandleAccess allowHandleAccess) {
        if (allowHandleAccess == null)
            throw new NullPointerException("Null access token. Method is intended for use by PositionerApi");

        if (!hasNativeHandle())
            throw new IllegalStateException("Native handle not available");

        return getNativeHandle();
    }

    /**
     * @eegeo.internal
     */
    @UiThread
    void setProjectedState(
            Point screenPoint,
            boolean isScreenPointValid,
            boolean isBehindGlobeHorizon
    ) {
        if (!m_screenPoint.equals(screenPoint) ||
                m_isScreenPointValid != isScreenPointValid ||
                m_isBehindGlobeHorizon != isBehindGlobeHorizon) {

            if(screenPoint!=null) {
                m_screenPoint.set(screenPoint.x, screenPoint.y);
            }

            m_isScreenPointValid = isScreenPointValid;
            m_isBehindGlobeHorizon = isBehindGlobeHorizon;
            if (m_positionerChangedListener != null) {
                m_positionerChangedListener.onPositionerChanged(this);
            }
        }
    }

    static final class AllowHandleAccess {
        @WorkerThread
        private AllowHandleAccess() {
        }
    }
}