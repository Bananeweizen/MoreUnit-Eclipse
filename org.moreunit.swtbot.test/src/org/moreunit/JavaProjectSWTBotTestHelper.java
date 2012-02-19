/**
 * 
 */
package org.moreunit;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.ChildrenControlFinder;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.moreunit.test.context.TestContextRule;

/**
 * @author gianasista
 *
 */
public class JavaProjectSWTBotTestHelper 
{
	@Rule
	public final TestContextRule context = new TestContextRule();
	
	protected static SWTWorkbenchBot bot;
	private static boolean isWorkspacePrepared;
	
	@BeforeClass
	public static void initialize() {
		bot = new SWTWorkbenchBot();
		
		if(!isWorkspacePrepared)
		{
			SWTBotPreferences.PLAYBACK_DELAY = 10;
			// Init keyboard layout
			SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
			switchToJavaPerspective();
			isWorkspacePrepared = true;
		}
	}
	
	/*
	 * Weired behaviour occurs, when editor do not get closed before and after each test,
	 * e.g. methods are missing from a class...
	 */
	@Before
	@After
	public void afterAndAfter()
	{
		for(SWTBotEditor editor : bot.editors())
		{
			editor.close();
		}
	}
	
	private static void switchToJavaPerspective() 
	{
		bot.menu("Window").menu("Open Perspective").menu("Other...").click();
		SWTBotShell openPerspectiveShell = bot.shell("Open Perspective");
		openPerspectiveShell.activate();

		// select the dialog
		bot.table().select("Java");
		bot.button("OK").click();
	}
	
	protected void openResource(String resourceName)
	{
		KeyboardFactory.getAWTKeyboard().pressShortcut(SWT.COMMAND | SWT.SHIFT, 'r');
    	SWTBotShell openTypeShell = bot.shell("Open Resource");
    	assertThat(openTypeShell).isNotNull();
    	openTypeShell.activate();
    	SWTBotText searchField = new SWTBotText(bot.widget(widgetOfType(Text.class)));
    	searchField.typeText(resourceName);
    	
    	bot.waitUntil(new DefaultCondition() {
			
			@Override
			public boolean test() throws Exception {
				return bot.table().rowCount() > 0;
			}
			
			@Override
			public String getFailureMessage() {
				return null;
			}
		});
    	KeyboardFactory.getAWTKeyboard().pressShortcut(Keystrokes.DOWN);
    	bot.button("Open").click();
    	bot.waitUntilWidgetAppears(new DefaultCondition() {
			
			@Override
			public boolean test() throws Exception {
				return !new SWTWorkbenchBot().editors().isEmpty();
			}
			
			@Override
			public String getFailureMessage() {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}

	protected void pressJumpShortcut() 
	{
		KeyboardFactory.getAWTKeyboard().pressShortcut(SWT.CTRL, 'j');
	}

	protected void waitForChooseDialog() 
	{
		bot.waitUntil(new DefaultCondition() 
		{
			@Override
			public boolean test() throws Exception 
			{
				return bot.activeShell() != null;
			}
			
			@Override
			public String getFailureMessage() 
			{
				return "ChooseDialog did not appear.";
			}
		});
	}

	protected IJavaProject getJavaProjectFromContext() 
	{
		return context.getProjectHandler().get();
	}

	protected SWTBotTreeItem selectAndReturnJavaProjectFromPackageExplorer() 
	{
		SWTBotView packageExplorerView = bot.viewByTitle("Package Explorer");
	
		List<Tree> findControls = new ChildrenControlFinder(packageExplorerView.getWidget()).findControls(WidgetOfType.widgetOfType(Tree.class));
		if (findControls.isEmpty())
			fail("Tree in Package Explorer View was not found.");
	
		SWTBotTree tree = new SWTBotTree((Tree) findControls.get(0));
		
		
		SWTBotTreeItem projectNode = tree.expandNode(getProjectNameFromContext());
		tree.select(projectNode);
		
		return projectNode;
	}
	
	private String getProjectNameFromContext() 
	{
		return context.getProjectHandler().get().getElementName();
	}
}
