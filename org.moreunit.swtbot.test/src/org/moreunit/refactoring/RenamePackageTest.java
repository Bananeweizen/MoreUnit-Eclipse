package org.moreunit.refactoring;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.moreunit.JavaProjectSWTBotTestHelper;
import org.moreunit.test.context.Context;

/**
 * @author gianasista
 */
@Context(mainCls = "org:SomeClass")
public class RenamePackageTest extends JavaProjectSWTBotTestHelper 
{
	/*
	 * This should be a testcase for:
	 * http://sourceforge.net/tracker/?func=detail&aid=3285663&group_id=156007&atid=798056
	 * 
	 * Renaming a package with use of package prefix and suffix
	 */
	@Test
	public void should_not_throw_exception_when_renaming_package_while_using_package_prefix_and_suffix() throws Exception 
	{
		try 
		{
			SWTBotTreeItem packageToRename = selectAndReturnPackage();

			// Press rename shortcut
			packageToRename.pressShortcut(SWT.ALT | SWT.COMMAND, 'r');
			
			// fill dialog to rename package
			SWTBotText textWithLabel = bot.textWithLabel("New name:");
			assertThat(textWithLabel).isNotNull();
			textWithLabel.setText("some.name");
			bot.shell("Rename Package").activate();
			
			// start rename refactoring
			SWTBotButton okButton = bot.button("OK");
			assertThat(okButton).isNotNull();
			okButton.click();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			fail("Renaming a package must not throw an exception! ("+ e + ")");
		}
	}

	private SWTBotTreeItem selectAndReturnPackage() 
	{
		SWTBotTreeItem projectNode = selectAndReturnJavaProjectFromPackageExplorer();

		SWTBotTreeItem sourcesFolder = projectNode.getNode("src");
		sourcesFolder.select();
		sourcesFolder.expand();

		SWTBotTreeItem orgPackage = sourcesFolder.getNode("org");
		orgPackage.select();

		return orgPackage;
	}
}
