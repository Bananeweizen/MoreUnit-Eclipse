package org.moreunit.wizards;

import static org.fest.assertions.Assertions.assertThat;
import static org.moreunit.wizards.NewClassWizard.removePrefix;
import static org.moreunit.wizards.NewClassWizard.removeSuffix;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.moreunit.MoreUnitPlugin;
import org.moreunit.elements.SourceFolderMapping;
import org.moreunit.preferences.Preferences;
import org.moreunit.test.context.ContextTestCase;
import org.moreunit.test.context.Project;
import org.moreunit.test.context.Properties;
import org.moreunit.test.context.TestProject;
import org.moreunit.test.context.TestType;
import org.moreunit.test.workspace.ProjectHandler;
import org.moreunit.test.workspace.SourceFolderHandler;
import org.moreunit.test.workspace.TypeHandler;

public class NewTestCaseWizardTest  extends NewClassyWizardTestCase
{
    @Override
    protected Class< ? extends NewClassyWizard> getWizardClass()
    {
        return NewTestCaseWizard.class;
    }

    @Test
    @Project(
        mainSrcFolder = "main-src",
        testSrcFolder = "test-src",
        mainCls = "pack: Class",
        properties = @Properties(JUnit3WithVariousPrefixesAndSuffixes.class))
    public void should_create_test_case_in_test_source_folder_of_same_project() throws Exception
    {
        // given
        NewTestCaseWizard wizard = new NewTestCaseWizard(context.getPrimaryTypeHandler("pack.Class").get());

        willAutomaticallyValidateWhenOpen(wizard);

        // when
        IType createdType = wizard.open();

        // then
        ProjectHandler testProject = context.getProjectHandler();
        testProject.assertThat().hasSourceFolder("test-src");
        context.assertCompilationUnit("pre.pack.suf.SomClassTes").isInSourceFolder("test-src").hasPrimaryType(createdType);
    }
    
    @Test
    @Project(mainSrcFolder = "main-src", mainCls = "pack: Class")
    @org.moreunit.test.context.Preferences(JUnit3WithVariousPrefixesAndSuffixesPreferences.class)
    public void should_create_test_case_in_default_test_folder_of_same_project() throws Exception
    {
        // given
        NewTestCaseWizard wizard = new NewTestCaseWizard(context.getPrimaryTypeHandler("pack.Class").get());

        willAutomaticallyValidateWhenOpen(wizard);

        // when
        IType createdType = wizard.open();

        // then
        ProjectHandler testProject = context.getProjectHandler();
        testProject.assertThat().hasSourceFolder("default-test-src");
        context.assertCompilationUnit("pre.pack.suf.SomClassTes").isInSourceFolder("default-test-src").hasPrimaryType(createdType);
    }

    @Test
    @Project(
        mainSrcFolder = "main-src",
        mainCls = "pack: Class",
        properties = @Properties(JUnit3WithVariousPrefixesAndSuffixes.class),
        testProject = @TestProject(srcFolder = "test-src"))
    public void should_create_test_case_in_source_folder_of_test_project() throws Exception
    {
        // given
        NewTestCaseWizard wizard = new NewTestCaseWizard(context.getPrimaryTypeHandler("pack.Class").get());

        willAutomaticallyValidateWhenOpen(wizard);

        // when
        IType createdType = wizard.open();

        // then
        ProjectHandler testProject = context.getTestProjectHandler();
        testProject.assertThat().hasSourceFolder("test-src");
        context.assertCompilationUnit("pre.pack.suf.SomClassTes").isInProject(testProject).isInSourceFolder("test-src").hasPrimaryType(createdType);
    }

    @Test
    @Project(
        mainSrcFolder = "main-src",
        testSrcFolder = "test-src",
        properties = @Properties(JUnit3WithVariousPrefixesAndSuffixes.class))
    public void should_create_test_case_in_test_source_folder_associated_to_current_main_folder() throws Exception
    {
        // given
        ProjectHandler project = context.getProjectHandler();

        addMapping(project, project.getSrcFolderHandler("main-src2"), project.getSrcFolderHandler("test-src2"));

        NewTestCaseWizard wizard = new NewTestCaseWizard(project.getSrcFolderHandler("main-src2").createClass("pack.Class").get());

        willAutomaticallyValidateWhenOpen(wizard);

        // when
        IType createdType = wizard.open();

        // then
        project.assertThat().hasSourceFolder("test-src2");
        context.assertCompilationUnit("pre.pack.suf.SomClassTes").isInSourceFolder("test-src2").hasPrimaryType(createdType);
    }
}
