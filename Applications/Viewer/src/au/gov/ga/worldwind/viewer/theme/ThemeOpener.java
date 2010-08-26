package au.gov.ga.worldwind.viewer.theme;

import java.io.InputStream;
import java.net.URL;

import javax.swing.JOptionPane;

import au.gov.ga.worldwind.viewer.downloader.Downloader;
import au.gov.ga.worldwind.viewer.downloader.HttpException;
import au.gov.ga.worldwind.viewer.downloader.RetrievalResult;

public class ThemeOpener
{
	public static void openTheme(final URL url, final ThemeOpenDelegate delegate)
	{
		if (url == null)
			throw new IllegalArgumentException("Theme URL cannot be null");

		//TODO create loading frame

		final ThemeOpenDelegate innerDelegate = new ThemeOpenDelegate()
		{
			@Override
			public void opened(Theme theme)
			{
				//TODO close loading frame
				delegate.opened(theme);
			}
		};

		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					//RetrievalResult result = Downloader.downloadImmediatelyIfModified(url);
					RetrievalResult result = Downloader.downloadImmediately(url, false);
					InputStream is = result.getAsInputStream();
					Theme theme = ThemeFactory.createFromXML(is, url);
					if (theme == null)
						throw new Exception("Could not create theme from XML document");
					innerDelegate.opened(theme);
				}
				catch (Exception e)
				{
					//TODO if response is 403 (forbidden) or 407 (authentication required), allow setting of proxy?
					//for now, just display a message

					int response =
							(e instanceof HttpException) ? ((HttpException) e).getResponseCode()
									: -1;
					boolean forbidden = response == 403 || response == 407;
					String message =
							"Could not open theme "
									+ url
									+ ": "
									+ e.getLocalizedMessage()
									+ ".\n\nUsing default theme."
									+ (forbidden
											? " If you are behind a proxy server, please setup the proxy in Options/Preferences/Network."
											: "");
					JOptionPane
							.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
					openDefault(innerDelegate);
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	public static void openDefault(ThemeOpenDelegate delegate)
	{
		URL url = ThemeOpener.class.getResource("/config/DefaultTheme.xml");
		try
		{
			InputStream is = url.openStream();
			Theme theme = ThemeFactory.createFromXML(is, url);
			if (theme == null)
				throw new Exception("Could not create theme from XML document");
			delegate.opened(theme);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not open default theme: "
					+ e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static interface ThemeOpenDelegate
	{
		public void opened(Theme theme);
	}
}