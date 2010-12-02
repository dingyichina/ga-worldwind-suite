package au.gov.ga.worldwind.common.layers.point.types;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.common.layers.point.PointLayer;
import au.gov.ga.worldwind.common.layers.point.PointLayerHelper;
import au.gov.ga.worldwind.common.layers.point.PointProperties;
import au.gov.ga.worldwind.common.util.DefaultLauncher;

/**
 * {@link PointLayer} implementation which extends {@link MarkerLayer} and uses
 * Markers to represent points.
 * 
 * @author Michael de Hoog
 */
public class MarkerPointLayer extends MarkerLayer implements PointLayer, SelectListener
{
	private final PointLayerHelper helper;

	private List<Marker> markers = new ArrayList<Marker>();
	private UrlMarker pickedMarker;
	private Material highlightMaterial = new Material(Color.white);

	private GlobeAnnotation tooltipAnnotation;

	public MarkerPointLayer(PointLayerHelper helper)
	{
		this.helper = helper;

		// Init tooltip annotation
		this.tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(0, 0, 0));
		Font font = Font.decode("Arial-Plain-16");
		this.tooltipAnnotation.getAttributes().setFont(font);
		this.tooltipAnnotation.getAttributes().setSize(new Dimension(270, 0));
		this.tooltipAnnotation.getAttributes().setDistanceMinScale(1);
		this.tooltipAnnotation.getAttributes().setDistanceMaxScale(1);
		this.tooltipAnnotation.getAttributes().setVisible(false);
		this.tooltipAnnotation.setPickEnabled(false);
		this.tooltipAnnotation.setAlwaysOnTop(true);
	}

	@Override
	public void render(DrawContext dc)
	{
		if (isEnabled())
		{
			helper.requestPoints(this);
		}
		this.tooltipAnnotation.render(dc);
		super.render(dc);
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		wwd.addSelectListener(this);
	}

	@Override
	public Sector getSector()
	{
		return helper.getSector();
	}

	@Override
	public void addPoint(Position position, AVList attributeValues)
	{
		MarkerAttributes attributes = new BasicMarkerAttributes();
		PointProperties properties = helper.getStyle(attributeValues);
		properties.style.setPropertiesFromAttributes(helper.getContext(), attributeValues, attributes);
		fixShapeType(attributes);
		UrlMarker marker = new UrlMarker(position, attributes);
		marker.setUrl(properties.link);
		marker.setTooltipText(properties.text);
		markers.add(marker);
	}
	
	protected void fixShapeType(MarkerAttributes attributes)
	{
		//someone decided to use string equality instead of the equals function when testing the
		//shape type; so fix it here instead (see BasicMarkerShape.createShapeInstance()).
		
		String shapetype = attributes.getShapeType();
		if(shapetype != null)
		{
			if(BasicMarkerShape.SPHERE.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.SPHERE);
			else if(BasicMarkerShape.CONE.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.CONE);
			else if(BasicMarkerShape.CYLINDER.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.CYLINDER);
			else if(BasicMarkerShape.HEADING_ARROW.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.HEADING_ARROW);
			else if(BasicMarkerShape.HEADING_LINE.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.HEADING_LINE);
			else if(BasicMarkerShape.ORIENTED_SPHERE.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.ORIENTED_SPHERE);
			else if(BasicMarkerShape.ORIENTED_CONE.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.ORIENTED_CONE);
			else if(BasicMarkerShape.ORIENTED_CYLINDER.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.ORIENTED_CYLINDER);
			else if(BasicMarkerShape.ORIENTED_SPHERE_LINE.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.ORIENTED_SPHERE_LINE);
			else if(BasicMarkerShape.ORIENTED_CONE_LINE.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.ORIENTED_CONE_LINE);
			else if(BasicMarkerShape.ORIENTED_CYLINDER_LINE.equalsIgnoreCase(shapetype))
				attributes.setShapeType(BasicMarkerShape.ORIENTED_CYLINDER_LINE);
		}
	}

	@Override
	public void loadComplete()
	{
		setMarkers(markers);
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return helper.getUrl();
	}

	@Override
	public String getDataCacheName()
	{
		return helper.getDataCacheName();
	}

	@Override
	public void selected(SelectEvent e)
	{
		if (e == null)
			return;

		PickedObject topPickedObject = e.getTopPickedObject();
		if (topPickedObject != null && topPickedObject.getObject() instanceof UrlMarker)
		{
			if (pickedMarker != null)
			{
				highlight(pickedMarker, false);
			}

			pickedMarker = (UrlMarker) topPickedObject.getObject();
			highlight(pickedMarker, true);

			if (e.getEventAction() == SelectEvent.LEFT_CLICK)
			{
				String link = pickedMarker.getUrl();
				if (link != null)
				{
					try
					{
						URL url = new URL(link);
						DefaultLauncher.openURL(url);
					}
					catch (MalformedURLException m)
					{
					}
				}
			}
		}
		else if (pickedMarker != null)
		{
			highlight(pickedMarker, false);
			pickedMarker = null;
		}
	}

	protected void highlight(UrlMarker marker, boolean highlight)
	{
		if (highlight)
		{
			marker.backupMaterial();
			marker.getAttributes().setMaterial(highlightMaterial);
			this.tooltipAnnotation.setText(marker.getTooltipText());
			this.tooltipAnnotation.setPosition(marker.getPosition());
			this.tooltipAnnotation.getAttributes().setVisible(true);
		}
		else
		{
			marker.restoreMaterial();
			this.tooltipAnnotation.getAttributes().setVisible(false);
		}
	}
}
