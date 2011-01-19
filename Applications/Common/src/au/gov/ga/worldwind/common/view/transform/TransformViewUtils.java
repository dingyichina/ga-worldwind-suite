package au.gov.ga.worldwind.common.view.transform;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.BasicView;
import gov.nasa.worldwind.view.ViewUtil;

import java.util.LinkedList;

/**
 * Utilities used in the {@link TransformView}s
 */
public class TransformViewUtils
{
	protected static final double MINIMUM_NEAR_DISTANCE = 2;
    protected static final double MINIMUM_FAR_DISTANCE = 100;
	
    /**
     * @return The far clipping distance to use for the current eye position,
     * using the highest resolution elevation data for calculations
     */
    public static double computeFarClippingDistance(DrawContext dc)
    {
    	if (dc == null)
    	{
    		return MINIMUM_FAR_DISTANCE;
    	}
    	
    	Position eyePosition = dc.getView().getCurrentEyePosition();
    	if (eyePosition == null)
    	{
    		return MINIMUM_FAR_DISTANCE;
    	}
    	
    	double far = computeHorizonDistance(dc, eyePosition);
        return far < MINIMUM_FAR_DISTANCE ? MINIMUM_FAR_DISTANCE : far;
    }
    
    /**
     * @return The near clipping distance to use for the current eye position, 
     * using the highest resolution elevation data for calculations.
     * 
     * @see BasicView#computeNearClipDistance
     */
	public static double computeNearClippingDistance(DrawContext dc)
	{
		if (dc == null)
		{
			return MINIMUM_NEAR_DISTANCE;
		}
		
		Position eyePosition = dc.getView().getCurrentEyePosition();
		if (eyePosition == null)
		{
			return MINIMUM_NEAR_DISTANCE;
		}
		
        double elevation = computeElevationAboveSurface(dc, eyePosition);
        double tanHalfFov = dc.getView().getFieldOfView().tanHalfAngle();
        double near = elevation / (2 * Math.sqrt(2 * tanHalfFov * tanHalfFov + 1));
        
        return near < MINIMUM_NEAR_DISTANCE ? MINIMUM_NEAR_DISTANCE : near;
	}

	/**
	 * Computes the horizon distance from the current eye position
	 */
	public static double computeHorizonDistance(DrawContext dc, Position eyePosition)
    {
		if (dc == null || dc.getGlobe() == null || eyePosition == null)
		{
			return 0;
		}
		
		double elevation = eyePosition.getElevation();
		double elevationAboveSurface = computeElevationAboveSurface(dc, eyePosition);
		return ViewUtil.computeHorizonDistance(dc.getGlobe(), Math.max(elevation, elevationAboveSurface));
    }
	
	/**
	 * Computes the elevation of the provided eye position above the underlying terrain
	 * 
	 * @return The elevation, in metres.
	 */
	private static double computeElevationAboveSurface(DrawContext dc, Position eyePosition)
	{
		double surfaceElevation = computeBestElevationAtSurface(dc, eyePosition.latitude, eyePosition.longitude);
		return eyePosition.elevation - surfaceElevation;
	}

	/**
	 * Computes the elevation of the terrain surface at Lat/Lon location using the best resolution of the
	 * underlying elevation model.
	 * 
	 * @return The elevation, in metres.
	 */
	private static double computeBestElevationAtSurface(DrawContext dc, Angle latitude, Angle longitude)
	{
		Sector sec = new Sector(latitude, latitude, longitude, longitude);
		double bestRes = dc.getGlobe().getElevationModel().getBestResolution(sec);

		LinkedList<LatLon> in = new LinkedList<LatLon>();
		in.add(new LatLon(latitude, longitude));

		double[] out = new double[1];
		dc.getGlobe().getElevationModel().getElevations(sec, in, bestRes, out);

		Double tmp = out[0];
		if (tmp == dc.getGlobe().getElevationModel().getMissingDataSignal())
		{
			return dc.getGlobe().getElevationModel().getMissingDataReplacement();
		}
		return tmp;
	}
}
