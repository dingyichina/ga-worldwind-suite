package au.gov.ga.worldwind.dataset.downloader;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.retrieve.BasicRetrievalService;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Downloader
{
	private static final String DIRECTORY = "GA Viewer Cache"; //TODO centralize this?
	private static final Object cacheLock = new Object();

	//use the standard World Wind BasicRetrievalService for handling downloading
	private static final RetrievalService service = new BasicRetrievalService();
	private static final ActiveRetrieverCache retrieverCache = new ActiveRetrieverCache();

	/**
	 * Performs a download synchronously, returning the result immediately. If
	 * the URL is cached (and cache is true), no download is performed.
	 * 
	 * @param url
	 *            URL to download
	 * @param cache
	 *            Cache the result?
	 * @return Download result
	 * @throws Exception
	 *             If the download fails
	 */
	public static synchronized RetrievalResult downloadImmediately(final URL url,
			final boolean cache) throws Exception
	{
		//if the URL is cached, return it directly without downloading
		if (cache)
		{
			RetrievalResult result = getFromCache(url);
			if (result != null && result.hasData())
				return result;
		}

		ImmediateRetrievalHandler immediateHandler = new ImmediateRetrievalHandler();
		HandlerPostProcessor postProcessor = new HandlerPostProcessor(immediateHandler);
		URLRetriever retriever = createRetriever(url, null, postProcessor);

		//check if the request is a duplicate
		HandlerPostProcessor activeHandler = retrieverCache.getActiveRetriever(retriever);
		if (activeHandler != null)
		{
			activeHandler.addHandler(immediateHandler);
		}
		else
		{
			runRetriever(retriever, postProcessor);
		}

		//get the result immediately
		RetrievalResult result = immediateHandler.get();

		if (cache && result.hasData())
		{
			saveToCache(url, result);
		}

		return result;
	}

	/**
	 * Performs a download synchronously, returning the result immediately. If
	 * the URL is cached, the server is checked whether the URL has been
	 * modified since the last download. If so, the new result is downloaded,
	 * cached, and returned. Otherwise the cached result is returned.
	 * 
	 * @param url
	 *            URL to download
	 * @return Download result
	 * @throws Exception
	 *             If the download fails
	 */
	public static synchronized RetrievalResult downloadImmediatelyIfModified(final URL url)
			throws Exception
	{
		FileRetrievalResult cachedResult = getFromCache(url);
		Long lastModified = null;
		if (cachedResult != null && cachedResult.hasData())
			lastModified = cachedResult.lastModified();

		ImmediateRetrievalHandler immediateHandler = new ImmediateRetrievalHandler();
		HandlerPostProcessor postProcessor = new HandlerPostProcessor(immediateHandler);
		//download if lastModified is null or server's modification date is greater than lastModified
		URLRetriever retriever = createRetriever(url, lastModified, postProcessor);

		//check if the request is a duplicate
		HandlerPostProcessor activeHandler = retrieverCache.getActiveRetriever(retriever);
		if (activeHandler != null)
		{
			activeHandler.addHandler(immediateHandler);
		}
		else
		{
			runRetriever(retriever, postProcessor);
		}

		//get the result immediately
		RetrievalResult modifiedResult = immediateHandler.get();

		if (modifiedResult.hasData())
		{
			saveToCache(url, modifiedResult);
			return modifiedResult;
		}

		return cachedResult;
	}

	/**
	 * Performs a download asynchronously, calling the handler when download is
	 * complete. If the URL is cached, no download is performed.
	 * 
	 * @param url
	 *            URL to download
	 * @param downloadHandler
	 *            Handler to call when download is complete
	 * @param cache
	 *            Cache the result?
	 */
	public static synchronized void download(final URL url, final RetrievalHandler downloadHandler,
			final boolean cache)
	{
		if (cache)
		{
			RetrievalResult result = getFromCache(url);
			if (result != null && result.hasData())
			{
				downloadHandler.handle(result, true);
				return;
			}
		}

		RetrievalHandler cacherHandler = new RetrievalHandler()
		{
			@Override
			public void handle(RetrievalResult result, boolean cached)
			{
				if (cache && result.hasData())
				{
					saveToCache(url, result);
				}
				downloadHandler.handle(result, cached);
			}
		};

		HandlerPostProcessor postProcessor = new HandlerPostProcessor(cacherHandler);
		URLRetriever retriever = createRetriever(url, null, postProcessor);

		HandlerPostProcessor activeHandler = retrieverCache.getActiveRetriever(retriever);
		if (activeHandler != null)
		{
			activeHandler.addHandler(cacherHandler);
		}
		else
		{
			runRetriever(retriever, postProcessor);
		}
	}

	/**
	 * Performs a download asynchronously. If the URL is cached, the
	 * cacheHandler is called with the result. The server is checked to see if a
	 * newer version exists than the cached version. If so, it is downloaded,
	 * and the downloadHandler is called with the result (to check if new data
	 * has been downloaded, use result.hasData() in the downloadHandler).
	 * 
	 * @param url
	 *            URL to download
	 * @param cacheHandler
	 *            Handler to call with the result if the URL is cached
	 * @param downloadHandler
	 *            Handler to call after the download is complete
	 *            (result.hasData() will be false if the URL has not been
	 *            modified)
	 */
	public static void downloadIfModified(URL url, RetrievalHandler cacheHandler,
			RetrievalHandler downloadHandler)
	{
		download(url, cacheHandler, downloadHandler, true);
	}

	/**
	 * Performs a download asynchronously. If the URL is cached, the
	 * cacheHandler is called with the result. The latest version is also
	 * downloaded and cached, and the downloadHandler is called with the new
	 * result.
	 * 
	 * @param url
	 *            URL to download
	 * @param cacheHandler
	 *            Handler to call with the result if the URL is cached
	 * @param downloadHandler
	 *            Handler to call after the download is complete
	 */
	public static void downloadAnyway(URL url, RetrievalHandler cacheHandler,
			RetrievalHandler downloadHandler)
	{
		download(url, cacheHandler, downloadHandler, false);
	}

	private static synchronized void download(final URL url, final RetrievalHandler cacheHandler,
			final RetrievalHandler downloadHandler, final boolean checkIfModified)
	{
		FileRetrievalResult result = getFromCache(url);
		Long lastModified = null;
		if (result != null && result.hasData())
		{
			cacheHandler.handle(result, true);
			if (checkIfModified)
				lastModified = result.lastModified();
		}

		RetrievalHandler cacherHandler = new RetrievalHandler()
		{
			@Override
			public void handle(RetrievalResult result, boolean cached)
			{
				if (result.hasData())
				{
					saveToCache(url, result);
				}
				downloadHandler.handle(result, cached);
			}
		};

		HandlerPostProcessor postProcessor = new HandlerPostProcessor(cacherHandler);
		URLRetriever retriever = createRetriever(url, lastModified, postProcessor);

		HandlerPostProcessor currentHandler = retrieverCache.getActiveRetriever(retriever);
		if (currentHandler != null)
		{
			currentHandler.addHandler(cacherHandler);
		}
		else
		{
			runRetriever(retriever, postProcessor);
		}
	}

	private static FileRetrievalResult getFromCache(URL url)
	{
		synchronized (cacheLock)
		{
			URL fileUrl = getCacheURL(url);
			if (fileUrl != null)
			{
				try
				{
					File file = new File(fileUrl.toURI());
					return new FileRetrievalResult(file);
				}
				catch (Exception e)
				{
				}
			}
			return null;
		}
	}

	private static void saveToCache(URL url, RetrievalResult result)
	{
		synchronized (cacheLock)
		{
			try
			{
				File file = newCacheFile(url);
				WWIO.saveBuffer(result.getAsBuffer(), file);
				//note: the following is only available in Java 6
				file.setReadable(true, false);
				file.setWritable(true, false);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/*private static void removeCachedURL(URL url)
	{
		synchronized (cacheLock)
		{
			URL fileUrl = getCacheURL(url);
			WorldWind.getDataFileStore().removeFile(fileUrl);
		}
	}*/

	private static URL getCacheURL(URL url)
	{
		String filename = filenameForURL(url);
		return WorldWind.getDataFileStore().findFile(filename, false);
	}

	private static File newCacheFile(URL url)
	{
		String filename = filenameForURL(url);
		File file = WorldWind.getDataFileStore().newFile(filename);
		return file;
	}

	private static String filenameForURL(URL url)
	{
		// need to replace the following characters: \/:*?"<>|
		String external = url.toExternalForm();
		return DIRECTORY + File.separator + external.replaceAll("[\\/:*?\"<>|]", "_");
	}

	private static URLRetriever createRetriever(URL url, Long ifModifiedSince,
			RetrievalPostProcessor postProcessor)
	{
		if ("http".equalsIgnoreCase(url.getProtocol())
				|| "https".equalsIgnoreCase(url.getProtocol()))
			return new IfModifiedHTTPRetriever(url, ifModifiedSince, postProcessor);
		return new FileRetriever(url, ifModifiedSince, postProcessor);
	}

	private static void runRetriever(Retriever retriever, HandlerPostProcessor postProcessor)
	{
		retrieverCache.addRetriever(retriever, postProcessor);
		service.runRetriever(retriever);
	}

	private static class ImmediateRetrievalHandler implements RetrievalHandler
	{
		private Object semaphore = new Object();
		private RetrievalResult result;

		public RetrievalResult get()
		{
			synchronized (semaphore)
			{
				while (result == null)
				{
					try
					{
						semaphore.wait();
					}
					catch (InterruptedException e)
					{
					}
				}
			}
			return result;
		}

		@Override
		public void handle(RetrievalResult result, boolean cached)
		{
			synchronized (semaphore)
			{
				this.result = result;
				semaphore.notify();
			}
		}
	}

	private static class HandlerPostProcessor implements RetrievalPostProcessor
	{
		private Object lock = new Object();
		private final List<RetrievalHandler> handlers = new ArrayList<RetrievalHandler>();
		private RetrievalResult result = null;

		public HandlerPostProcessor(RetrievalHandler handler)
		{
			handlers.add(handler);
		}

		public void addHandler(RetrievalHandler handler)
		{
			synchronized (lock)
			{
				if (result == null)
					//result has not been calculated yet, so wait for run() to be called
					handlers.add(handler);
				else
					handler.handle(result, false);
			}
		}

		@Override
		public ByteBuffer run(Retriever retriever)
		{
			synchronized (lock)
			{
				ByteBuffer buffer = retriever.getBuffer();
				result = new ByteBufferRetrievalResult(buffer);
				for (RetrievalHandler handler : handlers)
					handler.handle(result, false);
				return buffer;
			}
		}
	}

	private static class ActiveRetrieverCache
	{
		private Object lock = new Object();
		private final Map<Retriever, HandlerPostProcessor> activeRetrievers =
				new HashMap<Retriever, HandlerPostProcessor>();

		public ActiveRetrieverCache()
		{
			//daemon thread to remove activeRetrievers when they are no longer active
			//in the retrieval service
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							Thread.sleep(5000);
						}
						catch (InterruptedException e)
						{
						}
						removeNonActiveRetrievers();
					}
				}
			});
			thread.setDaemon(true);
			thread.start();
		}

		private synchronized void removeNonActiveRetrievers()
		{
			synchronized (lock)
			{
				if (service.getNumRetrieversPending() <= 0)
					activeRetrievers.clear();
				else
				{
					List<Retriever> toRemove = new ArrayList<Retriever>();
					for (Retriever r : activeRetrievers.keySet())
						if (!service.contains(r))
							toRemove.add(r);
					for (Retriever r : toRemove)
						activeRetrievers.remove(r);
				}
			}
		}

		public HandlerPostProcessor getActiveRetriever(Retriever retriever)
		{
			synchronized (lock)
			{
				if (activeRetrievers.containsKey(retriever))
					return activeRetrievers.get(retriever);
				return null;
			}
		}

		public void addRetriever(Retriever retriever, HandlerPostProcessor postProcessor)
		{
			synchronized (lock)
			{
				activeRetrievers.put(retriever, postProcessor);
			}
		}
	}
}