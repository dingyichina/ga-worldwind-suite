package au.gov.ga.worldwind.viewer.application;

import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getVideocardFailureMessageKey;
import static au.gov.ga.worldwind.common.util.message.CommonMessageConstants.getVideocardFailureTitleKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.viewer.data.messages.ViewerMessageConstants.*;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.terrain.Tessellator;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import nasa.worldwind.retrieve.ExtendedRetrievalService;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.downloader.DownloaderStatusBar;
import au.gov.ga.worldwind.common.terrain.ElevationModelFactory;
import au.gov.ga.worldwind.common.terrain.WireframeRectangularTessellator;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.HtmlViewer;
import au.gov.ga.worldwind.common.ui.SelectableAction;
import au.gov.ga.worldwind.common.ui.SplashScreen;
import au.gov.ga.worldwind.common.ui.SwingUtil;
import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.common.util.DoubleClickZoomListener;
import au.gov.ga.worldwind.common.util.GASandpit;
import au.gov.ga.worldwind.common.util.GDALDataHelper;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.URLTransformer;
import au.gov.ga.worldwind.common.view.stereo.StereoOrbitView;
import au.gov.ga.worldwind.viewer.components.locallayer.LocalLayerCreator;
import au.gov.ga.worldwind.viewer.components.sectorclipper.SectorClipper;
import au.gov.ga.worldwind.viewer.components.sectorsaver.ImageSectorSaver;
import au.gov.ga.worldwind.viewer.layers.ViewerLayerFactory;
import au.gov.ga.worldwind.viewer.layers.mouse.MouseLayer;
import au.gov.ga.worldwind.viewer.panels.SideBar;
import au.gov.ga.worldwind.viewer.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.viewer.panels.layers.AbstractLayersPanel;
import au.gov.ga.worldwind.viewer.panels.layers.ExtendedLayerList;
import au.gov.ga.worldwind.viewer.panels.layers.ILayerNode;
import au.gov.ga.worldwind.viewer.panels.layers.LayerEnabler;
import au.gov.ga.worldwind.viewer.panels.layers.LayerNode;
import au.gov.ga.worldwind.viewer.panels.layers.LayersPanel;
import au.gov.ga.worldwind.viewer.panels.layers.QueryClickListener;
import au.gov.ga.worldwind.viewer.panels.other.GoToCoordinatePanel;
import au.gov.ga.worldwind.viewer.retrieve.PolylineLayerRetrievalListener;
import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.settings.SettingsDialog;
import au.gov.ga.worldwind.viewer.stereo.StereoSceneController;
import au.gov.ga.worldwind.viewer.terrain.SectionListCompoundElevationModel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemeFactory;
import au.gov.ga.worldwind.viewer.theme.ThemeHUD;
import au.gov.ga.worldwind.viewer.theme.ThemeLayer;
import au.gov.ga.worldwind.viewer.theme.ThemeOpener;
import au.gov.ga.worldwind.viewer.theme.ThemeOpener.ThemeOpenDelegate;
import au.gov.ga.worldwind.viewer.theme.ThemePanel;
import au.gov.ga.worldwind.viewer.theme.ThemePiece;
import au.gov.ga.worldwind.viewer.theme.ThemePiece.ThemePieceAdapter;
import au.gov.ga.worldwind.viewer.theme.hud.WorldMapHUD;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;
import au.gov.ga.worldwind.wmsbrowser.WmsBrowser;
import au.gov.ga.worldwind.wmsbrowser.WmsLayerReceiver;

public class Application
{
	private static final String HELP_URL = getMessage(getHelpUrlKey());

	private static URL themeUrl;
	private static Element themeElement;

	static
	{
		if (Configuration.isMacOS())
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Viewer");
			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
			System.setProperty("apple.awt.brushMetalLook", "true");
		}

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}

		Configuration.setValue(AVKey.LAYER_FACTORY, ViewerLayerFactory.class.getName());
		Configuration.setValue(AVKey.ELEVATION_MODEL_FACTORY, ElevationModelFactory.class.getName());
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, StereoSceneController.class.getName());
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, StereoOrbitView.class.getName());
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, ExtendedRetrievalService.class.getName());
		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, WireframeRectangularTessellator.class.getName());
		//Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, RectangularTessellatorAccessible.class.getName());
		
		GDALDataHelper.init();
	}

	public static void main(String[] args)
	{
		int argsLength = args.length;

		String lastArg = argsLength > 0 ? args[argsLength - 1].toLowerCase().trim() : null;
		boolean sandpit = "-sandpit".equals(lastArg) || "--sandpit".equals(lastArg);
		GASandpit.setSandpitMode(sandpit);
		if (sandpit)
		{
			argsLength--; //ensure the sandpit argument is not used for the theme URL
		}

		//Settings need to be initialised before Theme is opened, so that proxy values are set
		Settings.init();

		URL themeUrl = null;
		if (argsLength > 0)
		{
			//first try the argument as a url
			try
			{
				themeUrl = new URL(args[0]);
			}
			catch (MalformedURLException e)
			{
			}
		}
		if (themeUrl == null)
		{
			File file = null;
			if (argsLength > 0)
			{
				//next try the argument as a filename
				file = new File(args[0]);
			}
			else
			{
				//finally search for theme.xml in the current directory
				file = new File("theme.xml");
				if (!file.exists())
				{
					file = null;
				}
			}

			if (file != null)
			{
				try
				{
					themeUrl = file.toURI().toURL();
				}
				catch (MalformedURLException e)
				{
				}
			}
		}

		ThemeOpenDelegate delegate = new ThemeOpenDelegate()
		{
			@Override
			public void opened(Theme theme, Element themeElement, URL themeUrl)
			{
				Application.themeElement = themeElement;
				Application.themeUrl = themeUrl;
				start(theme, false, true);
			}
		};
		if (themeUrl == null)
		{
			ThemeOpener.openDefault(delegate);
		}
		else
		{
			ThemeOpener.openTheme(themeUrl, delegate);
		}
	}

	private static Application restart(boolean fullscreen)
	{
		Theme theme = ThemeFactory.createFromXML(themeElement, themeUrl);
		return start(theme, fullscreen, false);
	}

	private static Application start(Theme theme, boolean fullscreen, boolean showSplashScreen)
	{
		if (theme.getInitialLatitude() != null)
		{
			Configuration.setValue(AVKey.INITIAL_LATITUDE, theme.getInitialLatitude());
		}
		if (theme.getInitialLongitude() != null)
		{
			Configuration.setValue(AVKey.INITIAL_LONGITUDE, theme.getInitialLongitude());
		}
		if (theme.getInitialAltitude() != null)
		{
			Configuration.setValue(AVKey.INITIAL_ALTITUDE, theme.getInitialAltitude());
		}
		if (theme.getInitialHeading() != null)
		{
			Configuration.setValue(AVKey.INITIAL_HEADING, theme.getInitialHeading());
		}
		if (theme.getInitialPitch() != null)
		{
			Configuration.setValue(AVKey.INITIAL_PITCH, theme.getInitialPitch());
		}

		if (theme.getCacheLocations() != null)
		{
			for (String location : theme.getCacheLocations())
			{
				WorldWind.getDataFileStore().addLocation(location, false);
			}
		}

		return new Application(theme, fullscreen, showSplashScreen);
	}

	private final boolean fullscreen;
	private Theme theme;
	private JFrame frame;
	private JFrame fullscreenFrame;

	private WorldWindowGLCanvas wwd;
	private MouseLayer mouseLayer;

	private SideBar sideBar;
	private DownloaderStatusBar statusBar;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private JSplitPane splitPane;

	private BasicAction openLayerAction;
	private BasicAction createLayerFromDirectoryAction;
	private SelectableAction offlineAction;
	private BasicAction screenshotAction;
	private BasicAction exitAction;
	private BasicAction defaultViewAction;
	private BasicAction gotoAction;
	private BasicAction fullscreenAction;
	private List<SelectableAction> hudActions = new ArrayList<SelectableAction>();
	private List<SelectableAction> panelActions = new ArrayList<SelectableAction>();
	private SelectableAction skirtAction;
	private SelectableAction wireframeAction;
	private SelectableAction wireframeDepthAction;
	private BasicAction settingsAction;
	private BasicAction helpAction;
	private BasicAction controlsAction;
	private BasicAction aboutAction;
	private BasicAction saveSectorAction;
	private BasicAction clipSectorAction;
	private BasicAction clearClipAction;
	private BasicAction wmsBrowserAction;

	private WmsBrowser wmsBrowser;

	private Application(Theme theme, final boolean fullscreen, boolean showSplashScreen)
	{
		this.theme = theme;
		this.fullscreen = fullscreen;
		Settings.get().loadThemeProperties(theme);

		//initialize frame
		String title = getMessage(getApplicationTitleKey());
		if (theme.getName() != null && theme.getName().length() > 0)
		{
			title += " - " + theme.getName();
		}
		if (GASandpit.isSandpitMode())
		{
			String sandpit = getMessage(getApplicationTitleSandpitSuffixKey());
			if (sandpit != null && sandpit.length() > 0)
			{
				title += " " + sandpit;
			}
		}
		frame = new JFrame(title);
		frame.setIconImage(Icons.earth32.getIcon().getImage());

		// show splashscreen
		final SplashScreen splashScreen = showSplashScreen ? new SplashScreen(frame) : null;

		// create worldwind stuff
		if(Settings.get().isHardwareStereoEnabled())
		{
			System.setProperty(AVKey.STEREO_MODE, "device");
		}
		wwd = new WorldWindowGLCanvas();
		wwd.setMinimumSize(new Dimension(1, 1));
		if (splashScreen != null)
		{
			splashScreen.addRenderingListener(wwd);
		}

		Model model = new BasicModel();
		model.setLayers(new ExtendedLayerList());
		model.getGlobe().setElevationModel(new SectionListCompoundElevationModel());
		wwd.setModel(model);
		wwd.addSelectListener(new ClickAndGoSelectListener(wwd, WorldMapLayer.class));
		create3DMouse();
		createDoubleClickListener();

		wwd.addRenderingExceptionListener(new RenderingExceptionListener()
		{
			@Override
			public void exceptionThrown(Throwable t)
			{
				if (t instanceof WWAbsentRequirementException)
				{
					String title = getMessage(getVideocardFailureTitleKey());
					String message = getMessage(getVideocardFailureMessageKey(), t.getMessage());
					JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
			}
		});

		//setup retrieval service layer
		RetrievalService rs = WorldWind.getRetrievalService();
		if (rs instanceof ExtendedRetrievalService)
		{
			PolylineLayerRetrievalListener layer = new PolylineLayerRetrievalListener();
			model.getLayers().add(layer);
			((ExtendedRetrievalService) rs).addRetrievalListener(layer);
		}

		//ensure menu bar and popups appear over the heavyweight WW canvas
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);

		frame.setBounds(Settings.get().getWindowBounds());
		if (!fullscreen && Settings.get().isWindowMaximized())
		{
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}

		if (fullscreen)
		{
			fullscreenFrame = new JFrame(frame.getTitle());
			fullscreenFrame.setIconImage(frame.getIconImage());
			fullscreenFrame.setUndecorated(true);
			fullscreenFrame.setAlwaysOnTop(true);

			JPanel fullscreenPanel = new JPanel(new BorderLayout());
			fullscreenFrame.setContentPane(fullscreenPanel);
			fullscreenPanel.add(wwd, BorderLayout.CENTER);

			fullscreenFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			fullscreenFrame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					setFullscreen(false);
				}
			});

			Action action = new AbstractAction()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setFullscreen(false);
				}
			};
			fullscreenPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), action);
			fullscreenPanel.getActionMap().put(action, action);


			boolean span = Settings.get().isSpanDisplays();
			String id = Settings.get().getDisplayId();
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Rectangle fullscreenBounds;
			Rectangle frameBounds = frame.getBounds();

			//the wwd is not in the frame, so shrink it to the width of the sidebar
			int splitLocation = Settings.get().getSplitLocation();
			frameBounds.width = splitLocation + 10;

			if (span)
			{
				Rectangle fullBounds = new Rectangle();
				GraphicsDevice[] gds = ge.getScreenDevices();
				for (GraphicsDevice g : gds)
				{
					GraphicsConfiguration gc = g.getDefaultConfiguration();
					fullBounds = fullBounds.union(gc.getBounds());
				}
				fullscreenBounds = fullBounds;
			}
			else
			{
				GraphicsDevice fullscreenDevice = getGraphicsDeviceForId(id, ge);
				fullscreenBounds = fullscreenDevice.getDefaultConfiguration().getBounds();

				//check if the frame bounds are within the fullscreen device
				//we want to show the frame on a different monitor to the fullscreen frame if possible
				GraphicsDevice frameDevice = getGraphicsDeviceContainingBounds(frameBounds, ge);
				if (frameDevice == fullscreenDevice)
				{
					GraphicsDevice otherDevice = getOtherGraphicsDevice(fullscreenDevice, ge);
					if (otherDevice != null)
					{
						Rectangle otherDeviceBounds = otherDevice.getDefaultConfiguration().getBounds();
						frameBounds.setLocation(otherDeviceBounds.getLocation());
					}
				}
			}

			frame.setBounds(frameBounds);
			fullscreenFrame.setBounds(fullscreenBounds);
		}

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		panel.add(splitPane, BorderLayout.CENTER);
		if (!fullscreen)
		{
			splitPane.setRightComponent(wwd);
		}
		splitPane.setOneTouchExpandable(true);
		loadSplitLocation();

		boolean anyPanels = !theme.getPanels().isEmpty();
		if (anyPanels)
		{
			sideBar = new SideBar(theme, splitPane);
			splitPane.setLeftComponent(sideBar);
		}

		if (!anyPanels || fullscreen)
		{
			splitPane.setDividerSize(0);
		}

		if (theme.hasStatusBar())
		{
			statusBar = new DownloaderStatusBar();
			panel.add(statusBar, BorderLayout.PAGE_END);
			statusBar.setEventSource(wwd);
			statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		}

		//link theme to WorldWindow
		theme.setup(frame, wwd);

		//if the theme has some theme layers defined, and the ThemeLayersPanel has not been
		//added, then we need to create a local LayerEnabler and enable the layers manually
		if (!theme.getLayers().isEmpty() && !theme.hasThemeLayersPanel())
		{
			List<ILayerNode> nodes = new ArrayList<ILayerNode>();
			for (ThemeLayer layer : theme.getLayers())
			{
				nodes.add(LayerNode.createFromLayerDefinition(layer));
			}

			LayerEnabler enabler = new LayerEnabler(theme.getLayersPanel().getTree(), wwd);
			enabler.enable(nodes);
		}

		afterSettingsChange();
		createActions();
		createThemeListeners();


		if (theme.hasMenuBar())
		{
			menuBar = createMenuBar();
			frame.setJMenuBar(menuBar);
		}

		if (theme.hasToolBar())
		{
			toolBar = createToolBar();
			panel.add(toolBar, BorderLayout.PAGE_START);
		}

		addWindowListeners();

		try
		{
			SwingUtil.invokeTaskOnEDT(new Runnable()
			{
				@Override
				public void run()
				{
					frame.setVisible(true);
					if (fullscreen)
					{
						fullscreenFrame.setVisible(true);
					}
					wwd.createBufferStrategy(2);
				}
			});
		}
		catch (Exception e)
		{
		}
		
		wmsBrowser = new WmsBrowser(getMessage(getApplicationTitleKey()));
		wmsBrowser.registerLayerReceiver(new WmsLayerReceiver()
		{
			@Override
			public void receive(WMSLayerInfo layerInfo)
			{
				addWmsLayer(layerInfo);
			}
		});
		
//		try
//		{
//			MutableScreenOverlayAttributesImpl attributes = new MutableScreenOverlayAttributesImpl(new URL("file:/c:/temp/demoSlide.html"));
//			attributes.setMinWidth("960px");
//			attributes.setMinHeight("720px");
//			ScreenOverlayLayer textLayer = new ScreenOverlayLayer(attributes);
//			wwd.getModel().getLayers().add(textLayer);
//		}
//		catch (Exception e)
//		{
//			
//		}
	}

	private void createActions()
	{
		openLayerAction = new BasicAction(getMessage(getOpenLayerActionLabelKey()), Icons.folder.getIcon());
		openLayerAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				openLayer();
			}
		});

		createLayerFromDirectoryAction = new BasicAction(getMessage(getCreateLayerFromDirectoryLabelKey()), Icons.newfolder.getIcon());
		createLayerFromDirectoryAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				createLayerFromDirectory();
			}
		});

		offlineAction = new SelectableAction(getMessage(getWorkOfflineLabelKey()), Icons.offline.getIcon(), WorldWind.isOfflineMode());
		offlineAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				WorldWind.setOfflineMode(offlineAction.isSelected());
			}
		});

		screenshotAction = new BasicAction(getMessage(getScreenshotLabelKey()), Icons.screenshot.getIcon());
		screenshotAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveImage();
			}
		});

		exitAction = new BasicAction(getMessage(getExitLabelKey()), Icons.escape.getIcon());
		exitAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				quit();
			}
		});

		defaultViewAction = new BasicAction(getMessage(getDefaultViewLabelKey()), Icons.home.getIcon());
		defaultViewAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				resetView();
			}
		});

		gotoAction = new BasicAction(getMessage(getGotoCoordsLabelKey()), Icons.crosshair45.getIcon());
		gotoAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				GoToCoordinatePanel.showGotoDialog(frame, wwd, getMessage(getGotoCoordsTitleKey()),
						Icons.crosshair45.getIcon());
			}
		});

		for (ThemePanel panel : theme.getPanels())
		{
			panelActions.add(createThemePieceAction(panel));
		}

		for (ThemeHUD hud : theme.getHUDs())
		{
			hudActions.add(createThemePieceAction(hud));
		}

		final Tessellator tess = wwd.getModel().getGlobe().getTessellator();
		skirtAction =
				new SelectableAction(getMessage(getRenderSkirtsLabelKey()), Icons.skirts.getIcon(),
						tess.isMakeTileSkirts());
		skirtAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				tess.setMakeTileSkirts(skirtAction.isSelected());
				wwd.redraw();
			}
		});

		wireframeAction =
				new SelectableAction(getMessage(getWireframeLabelKey()), Icons.wireframe.getIcon(), wwd.getModel()
						.isShowWireframeInterior());
		wireframeAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				wireframeDepthAction.setEnabled(wireframeAction.isSelected());
				wwd.getModel().setShowWireframeInterior(wireframeAction.isSelected());
				wwd.redraw();
			}
		});

		final WireframeRectangularTessellator tessellator = tess instanceof WireframeRectangularTessellator ? (WireframeRectangularTessellator) tess : null;
		boolean depth = tessellator != null && tessellator.isWireframeDepthTesting();
		wireframeDepthAction = new SelectableAction(getMessage(getWireframeDepthLabelKey()), Icons.zwireframe.getIcon(), depth);
		wireframeDepthAction.setEnabled(wireframeAction.isSelected());
		wireframeDepthAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				tessellator.setWireframeDepthTesting(wireframeDepthAction.isSelected());
				wwd.redraw();
			}
		});

		fullscreenAction = new BasicAction(getMessage(getFullscreenLabelKey()), Icons.monitor.getIcon());
		fullscreenAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFullscreen(!isFullscreen());
			}
		});

		settingsAction = new BasicAction(getMessage(getPreferencesLabelKey()), Icons.settings.getIcon());
		settingsAction.addActionListener(new ActionListener()
		{
			private boolean visible = false;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (!visible)
				{
					visible = true;
					SettingsDialog settingsDialog = new SettingsDialog(frame, getMessage(getPreferencesTitleKey()), Icons.settings.getIcon());
					settingsDialog.setVisible(true);
					visible = false;
					afterSettingsChange();
				}
			}
		});

		helpAction = new BasicAction(getMessage(getHelpLabelKey()), Icons.help.getIcon());
		helpAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					DefaultLauncher.openURL(URLTransformer.transform(new URL(HELP_URL)));
				}
				catch (MalformedURLException e1)
				{
				}
			}
		});

		controlsAction = new BasicAction(getMessage(getControlsLabelKey()), Icons.keyboard.getIcon());
		controlsAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				showControls();
			}
		});

		aboutAction = new BasicAction(getMessage(getAboutLabelKey()), Icons.about.getIcon());
		aboutAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				new AboutDialog(frame, getMessage(getAboutTitleKey()));
			}
		});

		saveSectorAction = new BasicAction(getMessage(getSaveSectorLabelKey()), Icons.save.getIcon());
		saveSectorAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveSector();
			}
		});

		clipSectorAction = new BasicAction(getMessage(getClipSectorLabelKey()), Icons.cut.getIcon());
		clipSectorAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				clipSector();
			}
		});

		clearClipAction = new BasicAction(getMessage(getClearClipLabelKey()), Icons.cutdelete.getIcon());
		clearClipAction.setEnabled(false);
		clearClipAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				clearClipping();
			}
		});
		
		wmsBrowserAction = new BasicAction(getMessage(getLaunchWmsBrowserLabelKey()), Icons.wmsbrowser.getIcon());
		wmsBrowserAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				showWmsBrowser();
			}
		});
	}

	private void openLayer()
	{
		LayersPanel panel = theme.getLayersPanel();
		if (panel != null)
		{
			panel.openLayerFile();
		}
	}

	private void createLayerFromDirectory()
	{
		LayersPanel panel = theme.getLayersPanel();
		if (panel != null)
		{
			ILayerDefinition layer = LocalLayerCreator.createDefinition(frame, createLayerFromDirectoryAction.getToolTipText(), panel.getIcon());
			if (layer != null)
			{
				panel.addLayer(layer);
			}
		}
	}

	private void saveImage()
	{
		String[] formats = ImageIO.getWriterFormatNames();
		final Set<String> imageFormats = new HashSet<String>();
		for (String format : formats)
		{
			imageFormats.add(format.toLowerCase());
		}

		JFileChooser chooser = new JFileChooser();
		List<FileFilter> filters = new ArrayList<FileFilter>();
		FileFilter jpgFilter = null;
		for (final String format : imageFormats)
		{
			FileFilter filter = new FileFilter()
			{
				@Override
				public boolean accept(File f)
				{
					if (f.isDirectory())
					{
						return true;
					}
					int index = f.getName().lastIndexOf('.');
					if (index < 0)
					{
						return false;
					}
					String ext = f.getName().substring(index + 1);
					return format.equals(ext.toLowerCase());
				}

				@Override
				public String getDescription()
				{
					return format.toUpperCase() + " " + getMessage(getTermImageKey());
				}

				@Override
				public String toString()
				{
					return format;
				}
			};
			filters.add(filter);
			chooser.addChoosableFileFilter(filter);
			if (format.equals("jpg"))
			{
				jpgFilter = filter;
			}
		}
		if (jpgFilter != null)
		{
			chooser.setFileFilter(jpgFilter);
		}

		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			// get filter extension
			String newExt;
			FileFilter filter = chooser.getFileFilter();
			if (filters.contains(filter))
			{
				newExt = filter.toString();
			}
			else
			{
				newExt = "jpg";
			}
			// find file extension
			int index = file.getName().lastIndexOf('.');
			String ext = null;
			if (index > 0)
			{
				ext = file.getName().substring(index + 1);
			}
			
			// fix/add file extension
			if (ext == null || !newExt.equals(ext.toLowerCase()))
			{
				ext = newExt;
				file = new File(file.getParent(), file.getName() + "." + ext);
			}
			// ask user if they want to overwrite
			if (file.exists())
			{
				int answer = JOptionPane.showConfirmDialog(frame,
								getMessage(getSaveImageOverwriteMessageKey(), file.getAbsolutePath()),
								getMessage(getSaveImageOverwriteTitleKey()), JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
				
				if (answer != JOptionPane.YES_OPTION)
				{
					file = null;
				}
			}
			if (file != null)
			{
				Screenshotter.takeScreenshot(wwd, wwd, file);
			}
		}
	}

	@SuppressWarnings("unused")
	private void takeScreenshot(int width, int height, final File file)
	{
		Screenshotter.takeScreenshot(wwd, width, height, file);
	}

	private void addWindowListeners()
	{
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (isFullscreen())
				{
					setFullscreen(false);
				}
				else
				{
					quit();
				}
			}
		});

		frame.addWindowStateListener(new WindowStateListener()
		{
			@Override
			public void windowStateChanged(WindowEvent e)
			{
				if (!isFullscreen())
				{
					Settings.get().setWindowMaximized(isMaximized());
				}
			}
		});

		frame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				if (!isMaximized() && !isFullscreen())
				{
					Settings.get().setWindowBounds(frame.getBounds());
				}
			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
				if (!isMaximized() && !isFullscreen())
				{
					Settings.get().setWindowBounds(frame.getBounds());
				}
			}
		});
	}

	private void resetView()
	{
		if (!(wwd.getView() instanceof OrbitView))
		{
			return;
		}

		OrbitView view = (OrbitView) wwd.getView();
		Position beginCenter = view.getCenterPosition();

		Double initLat = Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE);
		Double initLon = Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE);
		Double initAltitude = Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE);
		Double initHeading = Configuration.getDoubleValue(AVKey.INITIAL_HEADING);
		Double initPitch = Configuration.getDoubleValue(AVKey.INITIAL_PITCH);

		if (initLat == null)
		{
			initLat = 0d;
		}
		if (initLon == null)
		{
			initLon = 0d;
		}
		if (initAltitude == null)
		{
			initAltitude = 3d * Earth.WGS84_EQUATORIAL_RADIUS;
		}
		if (initHeading == null)
		{
			initHeading = 0d;
		}
		if (initPitch == null)
		{
			initPitch = 0d;
		}

		Position endCenter = Position.fromDegrees(initLat, initLon, beginCenter.getElevation());
		long lengthMillis = SettingsUtil.getScaledLengthMillis(beginCenter, endCenter);

		view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(view, beginCenter, endCenter,
				view.getHeading(), Angle.fromDegrees(initHeading), view.getPitch(), Angle.fromDegrees(initPitch),
				view.getZoom(), initAltitude, lengthMillis, WorldWind.ABSOLUTE));
		wwd.redraw();
	}

	private void create3DMouse()
	{
		final UserFacingIcon icon = new UserFacingIcon("au/gov/ga/worldwind/viewer/data/images/cursor.png", new Position(Angle.ZERO, Angle.ZERO, 0));
		icon.setSize(new Dimension(16, 32));
		icon.setAlwaysOnTop(true);

		LayerList layers = wwd.getModel().getLayers();
		mouseLayer = new MouseLayer(wwd, icon);
		layers.add(mouseLayer);

		enableMouseLayer();
	}

	private void createDoubleClickListener()
	{
		wwd.getInputHandler().addMouseListener(new DoubleClickZoomListener(wwd, 5000d));
	}

	private void enableMouseLayer()
	{
		mouseLayer.setEnabled(Settings.get().isStereoEnabled() && Settings.get().isStereoCursor());
	}

	public boolean isMaximized()
	{
		return (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
	}

	public boolean isFullscreen()
	{
		return fullscreen;
	}

	public void setFullscreen(final boolean fullscreen)
	{
		if (fullscreen != isFullscreen())
		{
			final JDialog dialog = new JDialog();
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.setLayout(new BorderLayout());
			String mode = fullscreen ? "fullscreen" : "windowed";
			JLabel label = new JLabel("Switching to " + mode + " mode, please wait...");
			label.setIcon(Icons.newLoadingIcon());
			label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			dialog.add(label, BorderLayout.CENTER);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);

			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					quit(false);
					Application application = restart(fullscreen);
					copyStateBetweenWorldWindows(wwd, application.wwd);
					dialog.dispose();
				}
			});
			thread.start();
		}
	}

	protected void copyStateBetweenWorldWindows(WorldWindowGLCanvas src, WorldWindowGLCanvas dst)
	{
		dst.setView(src.getView());
	}

	/**
	 * @return The graphics device corresponding to deviceId, or the default if
	 *         deviceId is null or a matching device could not be found.
	 */
	private GraphicsDevice getGraphicsDeviceForId(String deviceId, GraphicsEnvironment environment)
	{
		if (deviceId != null)
		{
			GraphicsDevice[] gds = environment.getScreenDevices();
			for (GraphicsDevice g : gds)
			{
				if (deviceId.equals(g.getIDstring()))
				{
					return g;
				}
			}
		}
		return environment.getDefaultScreenDevice();
	}

	private GraphicsDevice getOtherGraphicsDevice(GraphicsDevice device, GraphicsEnvironment environment)
	{
		GraphicsDevice[] gds = environment.getScreenDevices();
		for (GraphicsDevice g : gds)
		{
			if (g != device)
			{
				return g;
			}
		}
		return null;
	}

	private GraphicsDevice getGraphicsDeviceContainingBounds(Rectangle bounds, GraphicsEnvironment environment)
	{
		GraphicsDevice[] gds = environment.getScreenDevices();
		for (GraphicsDevice g : gds)
		{
			if (g.getDefaultConfiguration().getBounds().intersects(bounds))
			{
				return g;
			}
		}
		return null;
	}

	private JToolBar createToolBar()
	{
		JToolBar toolBar = new JToolBar();

		if (theme.hasLayersPanel())
		{
			toolBar.add(openLayerAction);
		}
		if (theme.hasWms())
		{
			toolBar.add(wmsBrowserAction);
		}
		if (theme.hasLayersPanel() || theme.hasWms())
		{
			toolBar.addSeparator();
		}
		
		toolBar.add(screenshotAction);

		toolBar.addSeparator();
		toolBar.add(defaultViewAction);
		toolBar.add(gotoAction);
		toolBar.add(fullscreenAction);

		toolBar.addSeparator();
		for (SelectableAction action : panelActions)
		{
			action.addToToolBar(toolBar);
		}

		toolBar.addSeparator();
		for (SelectableAction action : hudActions)
		{
			action.addToToolBar(toolBar);
		}

		toolBar.addSeparator();
		toolBar.add(settingsAction);

		return toolBar;
	}

	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu menu, submenu;

		menu = new JMenu(getMessage(getFileMenuLabelKey()));
		menuBar.add(menu);

		if (theme.hasLayersPanel())
		{
			menu.add(openLayerAction);

			submenu = new JMenu(getMessage(getCreateLayerMenuLabelKey()));
			submenu.setIcon(Icons.newfile.getIcon());
			menu.add(submenu);

			submenu.add(createLayerFromDirectoryAction);

			menu.addSeparator();
		}

		if (theme.hasWms())
		{
			menu.add(wmsBrowserAction);
			menu.addSeparator();
		}
		
		offlineAction.addToMenu(menu);

		menu.addSeparator();
		menu.add(screenshotAction);
		menu.add(saveSectorAction);

		menu.addSeparator();
		menu.add(exitAction);

		menu = new JMenu(getMessage(getViewMenuLabelKey()));
		menuBar.add(menu);

		menu.add(defaultViewAction);
		menu.add(gotoAction);
		menu.add(fullscreenAction);

		menu.addSeparator();
		menu.add(clipSectorAction);
		menu.add(clearClipAction);

		menu.addSeparator();
		skirtAction.addToMenu(menu);
		wireframeAction.addToMenu(menu);
		wireframeDepthAction.addToMenu(menu);

		menu.addSeparator();
		for (SelectableAction action : panelActions)
		{
			action.addToMenu(menu);
		}

		menu.addSeparator();
		for (SelectableAction action : hudActions)
		{
			action.addToMenu(menu);
		}

		menu = new JMenu(getMessage(getOptionsMenuLabelKey()));
		menuBar.add(menu);

		menu.add(settingsAction);

		menu = new JMenu(getMessage(getHelpMenuLabelKey()));
		menuBar.add(menu);

		menu.add(helpAction);
		menu.add(controlsAction);
		menu.add(aboutAction);

		return menuBar;
	}

	private SelectableAction createThemePieceAction(final ThemePiece piece)
	{
		final SelectableAction action = new SelectableAction(piece.getDisplayName(), piece.getIcon(), piece.isOn());
		action.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				piece.setOn(action.isSelected());
			}
		});
		piece.addListener(new ThemePieceAdapter()
		{
			@Override
			public void onToggled(ThemePiece source)
			{
				action.setSelected(source.isOn());
			}
		});
		return action;
	}

	private void showControls()
	{
		JDialog dialog =
				new HtmlViewer(frame, getMessage(getControlsTitleKey()), false,
						"/au/gov/ga/worldwind/viewer/data/help/controls.html", true);
		dialog.setResizable(false);
		dialog.setSize(640, 480);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}

	private void createThemeListeners()
	{
		for (ThemePanel panel : theme.getPanels())
		{
			if (panel instanceof AbstractLayersPanel)
			{
				AbstractLayersPanel layersPanel = (AbstractLayersPanel) panel;
				layersPanel.addQueryClickListener(new QueryClickListener()
				{
					@Override
					public void queryURLClicked(URL url)
					{
						initDataQuery(url);
					}
				});
			}
		}
	}

	private void initDataQuery(URL queryURL)
	{
		Position pos = ((OrbitView) wwd.getView()).getCenterPosition();
		double small = 1e-5;
		String bbox =
				(pos.getLongitude().degrees - small) + "," + (pos.getLatitude().degrees - small) + ","
						+ (pos.getLongitude().degrees + small) + "," + (pos.getLatitude().degrees + small);
		String external = queryURL.toExternalForm();
		String placeholder = "#bbox#";
		int index = external.indexOf(placeholder);
		if (index >= 0)
		{
			external = external.substring(0, index) + bbox + external.substring(index + placeholder.length());
		}

		try
		{
			URL url = new URL(external);
			HtmlViewer viewer = new HtmlViewer(frame, "Data", url, null);
			viewer.setSize(640, 480);
			viewer.setVisible(true);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	private void afterSettingsChange()
	{
		for (ThemeHUD hud : theme.getHUDs())
		{
			if (hud instanceof WorldMapHUD)
			{
				((WorldMapHUD) hud).setPickEnabled(!(Settings.get().isStereoEnabled() && Settings.get().isStereoCursor()));
			}
		}
		enableMouseLayer();
	}

	public void quit()
	{
		quit(true);
	}

	protected void quit(boolean systemExit)
	{
		saveSplitLocation();
		Settings.get().saveThemeProperties(theme);
		Settings.get().save();
		theme.dispose();
		frame.dispose();
		if (fullscreenFrame != null)
		{
			fullscreenFrame.dispose();
		}

		if (systemExit)
		{
			System.exit(0);
		}
	}

	private void saveSplitLocation()
	{
		if (sideBar != null && !isFullscreen())
		{
			if (sideBar.isVisible())
			{
				Settings.get().setSplitLocation(splitPane.getDividerLocation());
			}
			else
			{
				Settings.get().setSplitLocation(sideBar.getSavedDividerLocation());
			}
		}
	}

	private void loadSplitLocation()
	{
		splitPane.setDividerLocation(Settings.get().getSplitLocation());
	}

	private void saveSector()
	{
		ImageSectorSaver.beginSelection(frame, getMessage(getSaveSectorTitleKey()), wwd);
	}

	private void clipSector()
	{
		SectorClipper.beginSelection(frame, getMessage(getClipSectorTitleKey()), wwd, clipSectorAction, clearClipAction);
	}

	private void clearClipping()
	{
		StereoSceneController sceneController = (StereoSceneController) wwd.getSceneController();
		sceneController.clearClipping();
		wwd.redraw();
		clipSectorAction.setEnabled(true);
		clearClipAction.setEnabled(false);
	}
	
	private void showWmsBrowser()
	{
		wmsBrowser.show();
	}
	
	private void addWmsLayer(WMSLayerInfo layerInfo)
	{
		LayersPanel panel = theme.getLayersPanel();
		if (panel != null)
		{
			panel.addWmsLayer(layerInfo);
		}
	}
}
