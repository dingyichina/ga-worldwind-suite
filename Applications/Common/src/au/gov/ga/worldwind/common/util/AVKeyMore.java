package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.avlist.AVKey;

public interface AVKeyMore extends AVKey
{
	final static String CONTEXT_URL = "au.gov.ga.worldwind.AVKeyMore.ContextURL";
	final static String DELEGATE_KIT = "au.gov.ga.worldwind.AVKeyMore.DelegateKit";
	final static String DOWNLOADER_CONNECT_TIMEOUT = "au.gov.ga.worldwind.AVKeyMore.DownloaderConnectTimeout";
	final static String DOWNLOADER_READ_TIMEOUT = "au.gov.ga.worldwind.AVKeyMore.DownloaderReadTimeout";
	final static String EXPIRY_TIMESPAN = "au.gov.ga.worldwind.AVKeyMore.ExpiryTimespan";
	final static String EXPIRY_START_TIME = "au.gov.ga.worldwind.AVKeyMore.ExpiryStartTime";

	//curtain layer
	final static String FULL_WIDTH = "au.gov.ga.worldwind.AVKeyMore.FullWidth";
	final static String FULL_HEIGHT = "au.gov.ga.worldwind.AVKeyMore.FullHeight";
	final static String LEVEL_WIDTH = "au.gov.ga.worldwind.AVKeyMore.LevelWidth";
	final static String LEVEL_HEIGHT = "au.gov.ga.worldwind.AVKeyMore.LevelHeight";
	final static String POSITIONS = "au.gov.ga.worldwind.AVKeyMore.Positions";
	final static String CURTAIN_TOP = "au.gov.ga.worldwind.AVKeyMore.CurtainTop";
	final static String CURTAIN_BOTTOM = "au.gov.ga.worldwind.AVKeyMore.CurtainBottom";
	final static String FOLLOW_TERRAIN = "au.gov.ga.worldwind.AVKeyMore.FollowTerrain";
	final static String SUBSEGMENTS = "au.gov.ga.worldwind.AVKeyMore.Subsegments";
	final static String PATH = "au.gov.ga.worldwind.AVKeyMore.Path";

	//point layer
	final static String POINT_STYLES = "au.gov.ga.worldwind.AVKeyMore.PointStyles";
	final static String POINT_ATTRIBUTES = "au.gov.ga.worldwind.AVKeyMore.PointAttributes";
	final static String POINT_PROVIDER = "au.gov.ga.worldwind.AVKeyMore.PointProvider";
	final static String POINT_TYPE = "au.gov.ga.worldwind.AVKeyMore.PointType";

	//geometry layer
	final static String SHAPE_PROVIDER = "au.gov.ga.worldwind.AVKeyMore.ShapeProvider";
	final static String RENDER_TYPE = "au.gov.ga.worldwind.AVKeyMore.RenderType";
	final static String SHAPE_STYLES = "au.gov.ga.worldwind.AVKeyMore.ShapeStyles";
	final static String SHAPE_ATTRIBUTES = "au.gov.ga.worldwind.AVKeyMore.ShapeAttributes";
	final static String SHAPE_TYPE = "au.gov.ga.worldwind.AVKeyMore.ShapeType";
	
	//borehole layer
	final static String BOREHOLE_PROVIDER = "au.gov.ga.worldwind.AVKeyMore.BoreholeProvider";
	final static String BOREHOLE_STYLES = "au.gov.ga.worldwind.AVKeyMore.BoreholeStyles";
	final static String BOREHOLE_ATTRIBUTES = "au.gov.ga.worldwind.AVKeyMore.BoreholeAttributes";
	final static String BOREHOLE_SAMPLE_STYLES = "au.gov.ga.worldwind.AVKeyMore.BoreholeSampleStyles";
	final static String BOREHOLE_SAMPLE_ATTRIBUTES = "au.gov.ga.worldwind.AVKeyMore.BoreholeSampleAttributes";
	final static String BOREHOLE_UNIQUE_IDENTIFIER_ATTRIBUTE = "au.gov.ga.worldwind.AVKeyMore.BoreholeUniqueIdentifierAttribute";
	final static String BOREHOLE_SAMPLE_DEPTH_FROM_ATTRIBUTE = "au.gov.ga.worldwind.AVKeyMore.BoreholeSampleDepthFromAttribute";
	final static String BOREHOLE_SAMPLE_DEPTH_TO_ATTRIBUTE = "au.gov.ga.worldwind.AVKeyMore.BoreholeSampleDepthToAttribute";
	final static String BOREHOLE_LINE_WIDTH = "au.gov.ga.worldwind.AVKeyMore.BoreholeLineWidth";

	//historic earthquakes layer
	final static String COLORING = "au.gov.ga.worldwind.AVKeyMore.Coloring";
	final static String COLORING_MIN_DATE = "au.gov.ga.worldwind.AVKeyMore.ColoringMinDate";
	final static String COLORING_MAX_DATE = "au.gov.ga.worldwind.AVKeyMore.ColoringMaxDate";
	final static String POINT_SIZE = "au.gov.ga.worldwind.AVKeyMore.PointSize";
	
	//crust layer
	final static String SCALE = "au.gov.ga.worldwind.AVKeyMore.Scale";
	final static String WRAP = "au.gov.ga.worldwind.AVKeyMore.Wrap";
	
	//elevation model
	final static String EXTRACT_ZIP_ENTRY = "au.gov.ga.worldwind.AVKeyMore.ExtractZipEntry";
}
