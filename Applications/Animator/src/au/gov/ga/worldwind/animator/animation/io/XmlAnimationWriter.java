package au.gov.ga.worldwind.animator.animation.io;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An {@link AnimationWriter} that writes an {@link Animation} in the <em>version 2</em>
 * xml format.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class XmlAnimationWriter implements AnimationWriter
{

	private static final AnimationFileVersion CURRENT_FILE_VERSION = AnimationFileVersion.VERSION020;
	
	@Override
	public void writeAnimation(String fileName, Animation animation) throws IOException
	{
		Validate.notBlank(fileName, "A filename must be provided");
		Validate.notNull(animation, "An animation must be provided");
		
		File outputFile = new File(fileName);
		writeAnimation(outputFile, animation);
	}

	@Override
	public void writeAnimation(File file, Animation animation) throws IOException
	{
		Validate.notNull(file, "A file must be provided");
		Validate.notNull(animation, "An animation must be provided");
		
		Document document = WWXML.createDocumentBuilder(false).newDocument();
		
		Element rootElement = document.createElement("worldWindAnimation");
		document.appendChild(rootElement);
		
		WWXML.setTextAttribute(rootElement, "version", CURRENT_FILE_VERSION.getDisplayName());

		rootElement.appendChild(animation.toXml(rootElement));
		
		WWXML.saveDocumentToStream(document, new FileOutputStream(file));
	}


}