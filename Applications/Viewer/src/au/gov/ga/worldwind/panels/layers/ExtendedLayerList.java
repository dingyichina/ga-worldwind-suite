package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExtendedLayerList extends LayerList implements SectionList<Layer>
{
	private Map<Object, Layer> sectionMap = new HashMap<Object, Layer>();

	@Override
	public void registerSectionObject(Object section)
	{
		Layer layer = new DummyLayer();
		sectionMap.put(section, layer);
		add(layer);
	}

	@Override
	public void addAllFromSection(Object section, Collection<? extends Layer> c)
	{
		int index = -1;
		if (sectionMap.containsKey(section))
		{
			index = indexOf(sectionMap.get(section));
		}

		if (index < 0)
		{
			addAll(c);
			return;
		}

		addAll(index + 1, c);
	}

	@Override
	public void removeAllFromSection(Object section, Collection<? extends Layer> c)
	{
		removeAll(c);
	}

	private class DummyLayer extends AbstractLayer
	{
		@Override
		public void render(DrawContext dc)
		{
		}

		@Override
		public void pick(DrawContext dc, Point point)
		{
		}

		@Override
		public void preRender(DrawContext dc)
		{
		}

		@Override
		protected void doRender(DrawContext dc)
		{
		}
	}
}
