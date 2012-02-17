package au.gov.ga.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;

import java.nio.ByteOrder;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.ColorMap;
import au.gov.ga.worldwind.common.util.CoordinateTransformationUtil;

/**
 * Provides the ability to configure the {@link GocadReader}. An instance of
 * this class is provided to the {@link GocadFactory} when reading GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadReaderParameters
{
	private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
	private int voxetSubsamplingU = 1;
	private int voxetSubsamplingV = 1;
	private int voxetSubsamplingW = 1;
	private boolean voxetDynamicSubsampling = true;
	private int voxetDynamicSubsamplingSamplesPerAxis = 50;
	private boolean voxetBilinearMinification = true;
	private CoordinateTransformation coordinateTransformation = null;
	private ColorMap colorMap = null;

	public GocadReaderParameters()
	{
		//use defaults
	}

	/**
	 * Construct a new instance of this class, using the params to setup any
	 * default values.
	 * 
	 * @param params
	 *            Default parameters
	 */
	public GocadReaderParameters(AVList params)
	{
		ByteOrder bo = (ByteOrder) params.getValue(AVKey.BYTE_ORDER);
		if (bo != null)
			setByteOrder(bo);

		Integer i = (Integer) params.getValue(AVKeyMore.SUBSAMPLING_U);
		if (i != null)
			setVoxetSubsamplingU(i);

		i = (Integer) params.getValue(AVKeyMore.SUBSAMPLING_V);
		if (i != null)
			setVoxetSubsamplingV(i);

		i = (Integer) params.getValue(AVKeyMore.SUBSAMPLING_W);
		if (i != null)
			setVoxetSubsamplingW(i);

		Boolean b = (Boolean) params.getValue(AVKeyMore.DYNAMIC_SUBSAMPLING);
		if (b != null)
			setVoxetDynamicSubsampling(b);

		i = (Integer) params.getValue(AVKeyMore.DYNAMIC_SUBSAMPLING_SAMPLES_PER_AXIS);
		if (i != null)
			setVoxetDynamicSubsamplingSamplesPerAxis(i);

		b = (Boolean) params.getValue(AVKeyMore.BILINEAR_MINIFICATION);
		if (b != null)
			setVoxetBilinearMinification(b);

		String s = (String) params.getValue(AVKey.COORDINATE_SYSTEM);
		if (s != null)
			setCoordinateTransformation(CoordinateTransformationUtil.getTransformationToWGS84(s));

		ColorMap cm = (ColorMap) params.getValue(AVKeyMore.COLOR_MAP);
		if (cm != null)
			setColorMap(cm);
	}

	/**
	 * @return The amount of subsampling to use in the u-axis when reading GOCAD
	 *         voxets. Defaults to 1 (no subsampling).
	 */
	public int getVoxetSubsamplingU()
	{
		return voxetSubsamplingU;
	}

	/**
	 * Sets the amount of subsampling to use in the u-axis when reading GOCAD
	 * voxets.
	 * 
	 * @param voxetSubsamplingU
	 */
	public void setVoxetSubsamplingU(int voxetSubsamplingU)
	{
		this.voxetSubsamplingU = voxetSubsamplingU;
	}

	/**
	 * @return The amount of subsampling to use in the v-axis when reading GOCAD
	 *         voxets. Defaults to 1 (no subsampling).
	 */
	public int getVoxetSubsamplingV()
	{
		return voxetSubsamplingV;
	}

	/**
	 * Sets the amount of subsampling to use in the v-axis when reading GOCAD
	 * voxets.
	 * 
	 * @param voxetSubsamplingV
	 */
	public void setVoxetSubsamplingV(int voxetSubsamplingV)
	{
		this.voxetSubsamplingV = voxetSubsamplingV;
	}

	/**
	 * @return The amount of subsampling to use in the w-axis when reading GOCAD
	 *         voxets. Defaults to 1 (no subsampling).
	 */
	public int getVoxetSubsamplingW()
	{
		return voxetSubsamplingW;
	}

	/**
	 * Sets the amount of subsampling to use in the w-axis when reading GOCAD
	 * voxets.
	 * 
	 * @param voxetSubsamplingW
	 */
	public void setVoxetSubsamplingW(int voxetSubsamplingW)
	{
		this.voxetSubsamplingW = voxetSubsamplingW;
	}

	/**
	 * @return Should the reader use dynamic subsampling when reading GOCAD
	 *         voxets? Dynamic subsampling attempts to subsample the voxet
	 *         automatically to ensure a certain resolution (number of samples)
	 *         in each axis. Defaults to true.
	 */
	public boolean isVoxetDynamicSubsampling()
	{
		return voxetDynamicSubsampling;
	}

	/**
	 * Set whether the reader should use dynamic subsampling when reading GOCAD
	 * voxets.
	 * 
	 * @param voxetDynamicSubsampling
	 */
	public void setVoxetDynamicSubsampling(boolean voxetDynamicSubsampling)
	{
		this.voxetDynamicSubsampling = voxetDynamicSubsampling;
	}

	/**
	 * @return The number of samples to attempt to subsample to per axis when
	 *         dynamic subsampling is enabled. Defaults to 50.
	 */
	public int getVoxetDynamicSubsamplingSamplesPerAxis()
	{
		return voxetDynamicSubsamplingSamplesPerAxis;
	}

	/**
	 * Set the number of samples to subsample to per axis when using dynamic
	 * subsampling.
	 * 
	 * @param voxetDynamicSubsamplingSamplesPerAxis
	 */
	public void setVoxetDynamicSubsamplingSamplesPerAxis(int voxetDynamicSubsamplingSamplesPerAxis)
	{
		this.voxetDynamicSubsamplingSamplesPerAxis = voxetDynamicSubsamplingSamplesPerAxis;
	}

	/**
	 * @return Whether the reader should use bilinear minification when
	 *         subsampling GOCAD voxets. If false, a nearest neighbour approach
	 *         is used. Defaults to true.
	 */
	public boolean isVoxetBilinearMinification()
	{
		return voxetBilinearMinification;
	}

	/**
	 * Set whether the reader should use bilinear minification when subsampling
	 * GOCAD voxets.
	 * 
	 * @param voxetBilinearMinification
	 */
	public void setVoxetBilinearMinification(boolean voxetBilinearMinification)
	{
		this.voxetBilinearMinification = voxetBilinearMinification;
	}

	/**
	 * @return {@link ByteOrder} to use when reading binary GOCAD data (eg from
	 *         voxets). Defaults to {@link ByteOrder#LITTLE_ENDIAN}.
	 */
	public ByteOrder getByteOrder()
	{
		return byteOrder;
	}

	/**
	 * Set the {@link ByteOrder} to use when reading binary GOCAD data.
	 * 
	 * @param byteOrder
	 */
	public void setByteOrder(ByteOrder byteOrder)
	{
		this.byteOrder = byteOrder;
	}

	/**
	 * @return Map reprojection to use when reading GOCAD vertices (null for no
	 *         reprojection).
	 */
	public CoordinateTransformation getCoordinateTransformation()
	{
		return coordinateTransformation;
	}

	/**
	 * Set the map reprojection to use when reading GOCAD vertices.
	 * 
	 * @param coordinateTransformation
	 */
	public void setCoordinateTransformation(CoordinateTransformation coordinateTransformation)
	{
		this.coordinateTransformation = coordinateTransformation;
	}

	/**
	 * @return Colour map to use when assigning colours to GOCAD vertices.
	 */
	public ColorMap getColorMap()
	{
		return colorMap;
	}

	/**
	 * Set the colour map to use when assigning colours to GOCAD vertices.
	 * 
	 * @param colorMap
	 */
	public void setColorMap(ColorMap colorMap)
	{
		this.colorMap = colorMap;
	}
}