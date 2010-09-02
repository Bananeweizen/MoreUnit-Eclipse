package org.moreunit.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.ui.IEditorPart;
import org.moreunit.log.LogHandler;
import org.moreunit.preferences.Preferences;
import org.moreunit.ui.ChooseDialog;
import org.moreunit.ui.MemberContentProvider;
import org.moreunit.util.MethodCallFinder;
import org.moreunit.util.MethodTestCallerFinder;
import org.moreunit.util.TestCaseDiviner;
import org.moreunit.util.TestMethodDiviner;
import org.moreunit.util.TestMethodDivinerFactory;
import org.moreunit.wizards.NewTestCaseWizard;

/**
 * ClassTypeFacade offers easy access to a simple java file within eclipse. The
 * file represented by this instance is not a testcase.
 * 
 * @author vera 23.05.2006 20:28:52
 */
public class ClassTypeFacade extends TypeFacade
{

    private TestCaseDiviner testCaseDiviner;
    TestMethodDivinerFactory testMethodDivinerFactory;
    TestMethodDiviner testMethodDiviner;

    public ClassTypeFacade(ICompilationUnit compilationUnit)
    {
        super(compilationUnit);
        testMethodDivinerFactory = new TestMethodDivinerFactory(compilationUnit);
        testMethodDiviner = testMethodDivinerFactory.create();
    }

    public ClassTypeFacade(IEditorPart editorPart)
    {
        super(editorPart);
        testMethodDivinerFactory = new TestMethodDivinerFactory(compilationUnit);
        testMethodDiviner = testMethodDivinerFactory.create();
    }

    public ClassTypeFacade(IFile file)
    {
        super(file);
        testMethodDivinerFactory = new TestMethodDivinerFactory(compilationUnit);
        testMethodDiviner = testMethodDivinerFactory.create();
    }

    /**
     * Returns the corresponding testcase of the javaFileFacade. If there are
     * more than one testcases the user has to make a choice via a dialog. If no
     * test is found <code>null</code> is returned.
     * 
     * @return one of the corresponding testcases
     */
    public IType getOneCorrespondingTestCase(boolean createIfNecessary, boolean extendedSearch, String promptText)
    {
        Set<IType> testcases = getCorrespondingTestCaseList();
        Set<IType> additionalTestcases = new HashSet<IType>();
        if(extendedSearch)
        {
            additionalTestcases.addAll(getTestCasesHavingMethodsThatCallsMethodsOfThisClass());
        }

        IType testcaseToJump = null;
        int testcasesCount = testcases.size() + additionalTestcases.size();
        if(testcasesCount == 1)
        {
            testcases.addAll(additionalTestcases);
            testcaseToJump = (IType) testcases.toArray()[0];
        }
        else if(testcasesCount > 1)
        {
            MemberContentProvider contentProvider = new MemberContentProvider(testcases, additionalTestcases, null);
            testcaseToJump = new ChooseDialog<IType>(promptText, contentProvider).getChoice();
        }
        else if(createIfNecessary)
        {
            testcaseToJump = new NewTestCaseWizard(getType()).open();
        }

        return testcaseToJump;
    }

    private Collection<IType> getTestCasesHavingMethodsThatCallsMethodsOfThisClass()
    {
        Set<IType> testCases = new HashSet<IType>();
        try
        {
            for (IMethod method : this.compilationUnit.findPrimaryType().getMethods())
            {
                if((method.getFlags() & ClassFileConstants.AccPrivate) == 0)
                {
                    Set<IMethod> testMethods = getCallRelationshipFinder(method).getMatches(new NullProgressMonitor());
                    for (IMethod testMethod : testMethods)
                    {
                        testCases.add(testMethod.getDeclaringType());
                    }
                }
            }
        }
        catch (JavaModelException exc)
        {
            LogHandler.getInstance().handleExceptionLog(exc);
        }
        return testCases;
    }

    public Set<IType> getCorrespondingTestCaseList()
    {
        return getTestCaseDiviner().getMatches();
    }

    public IMethod getCorrespondingTestMethod(IMethod method, IType testCaseType)
    {
        String nameOfCorrespondingTestMethod = testMethodDiviner.getTestMethodNameFromMethodName(method.getElementName());

        if(testCaseType == null)
        {
            return null;
        }

        try
        {
            IMethod[] methodsOfType = testCaseType.getCompilationUnit().findPrimaryType().getMethods();
            for (IMethod testmethod : methodsOfType)
            {
                if(testmethod.getElementName().startsWith(nameOfCorrespondingTestMethod))
                {
                    return testmethod;
                }
            }
        }
        catch (JavaModelException exc)
        {
            LogHandler.getInstance().handleExceptionLog(exc);
        }

        return null;
    }

    public List<IMethod> getCorrespondingTestMethods(IMethod method)
    {
        Set<IType> allTestCases = getCorrespondingTestCaseList();
        return getTestMethodsForTestCases(method, allTestCases);
    }

    private List<IMethod> getTestMethodsForTestCases(IMethod method, Set<IType> testCases)
    {
        List<IMethod> result = new ArrayList<IMethod>();
        
        for (IType testCaseType : testCases)
        {
            result.addAll(getTestMethodsForTestCase(method, testCaseType));
        }

        return result;
    }

    private List<IMethod> getTestMethodsForTestCase(IMethod method, IType testCaseType)
    {
        List<IMethod> result = new ArrayList<IMethod>();

        if(testCaseType == null)
        {
            return result;
        }

        String nameOfCorrespondingTestMethod = testMethodDiviner.getTestMethodNameFromMethodName(method.getElementName());

        try
        {
            IMethod[] methodsOfType = testCaseType.getCompilationUnit().findPrimaryType().getMethods();
            for (IMethod testmethod : methodsOfType)
            {
                if(testmethod.getElementName().startsWith(nameOfCorrespondingTestMethod))
                {
                    result.add(testmethod);
                }
            }
        }
        catch (JavaModelException exc)
        {
            LogHandler.getInstance().handleExceptionLog(exc);
        }

        return result;
    }

    public boolean hasTestMethod(IMethod method)
    {
        List<IMethod> correspondingTestMethods = getCorrespondingTestMethods(method);
        return correspondingTestMethods != null && correspondingTestMethods.size() > 0;
    }

    /**
     * Getter uses lazy caching.
     */
    private TestCaseDiviner getTestCaseDiviner()
    {
        if(this.testCaseDiviner == null)
        {
            this.testCaseDiviner = new TestCaseDiviner(this.compilationUnit, Preferences.getInstance());
        }

        return this.testCaseDiviner;
    }

    @Override
    protected Set<IType> getCorrespondingClasses()
    {
        return new LinkedHashSet<IType>(getCorrespondingTestCaseList());
    }

    @Override
    protected Collection<IMethod> getCorrespondingMethodsInClasses(IMethod method, Set<IType> classes)
    {
        return getTestMethodsForTestCases(method, classes);
    }

    @Override
    protected MethodCallFinder getCallRelationshipFinder(IMethod method)
    {
        return new MethodTestCallerFinder(method);
    }

    public IType getOneCorrespondingTestCase(boolean createIfNecessary)
    {
        return getOneCorrespondingTestCase(createIfNecessary, false, "Please choose a test case...");
    }

}
