// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.helper;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.support.membermodification.MemberMatcher.*;
import static org.powermock.api.support.membermodification.MemberModifier.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import junit.framework.Assert;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.powermock.api.mockito.PowerMockito;
import org.talend.commons.bridge.ReponsitoryContextBridge;
import org.talend.commons.emf.EMFUtil;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.Status;
import org.talend.core.model.properties.User;
import org.talend.core.model.properties.helper.StatusHelper;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.i18n.Messages;
import org.talend.core.repository.model.FolderHelper;
import org.talend.core.repository.model.IRepositoryFactory;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.repository.utils.ProjectHelper;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.cwm.relational.RelationalFactory;
import org.talend.cwm.relational.TdExpression;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.manager.DQStructureManager;
import org.talend.dataprofiler.core.ui.utils.RepNodeUtils;
import org.talend.dataprofiler.core.ui.views.provider.RepositoryNodeBuilder;
import org.talend.dataprofiler.core.ui.views.resources.LocalRepositoryObjectCRUD;
import org.talend.dataquality.PluginConstant;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.AnalysisFactory;
import org.talend.dataquality.analysis.AnalysisResult;
import org.talend.dataquality.domain.pattern.Pattern;
import org.talend.dataquality.domain.pattern.PatternFactory;
import org.talend.dataquality.domain.pattern.RegularExpression;
import org.talend.dataquality.helpers.AnalysisHelper;
import org.talend.dataquality.helpers.ReportHelper;
import org.talend.dataquality.properties.TDQAnalysisItem;
import org.talend.dataquality.properties.TDQReportItem;
import org.talend.dataquality.properties.TDQSourceFileItem;
import org.talend.dataquality.properties.impl.PropertiesFactoryImpl;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.nodes.ReportFolderRepNode;
import org.talend.dq.nodes.ReportRepNode;
import org.talend.dq.nodes.ReportSubFolderRepNode;
import org.talend.dq.nodes.SourceFileFolderRepNode;
import org.talend.dq.nodes.SourceFileRepNode;
import org.talend.dq.nodes.SourceFileSubFolderNode;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.resource.EResourceConstant;
import org.talend.resource.ResourceManager;
import orgomg.cwmx.analysis.informationreporting.Report;

import common.Logger;

/**
 * created by yyin on 2012-8-22 Detailled comment: include some structure methods which can be used for any tests who
 * need related object. some are mocked object with the start "mock" on its method name; some are real created object
 * with the start "createReal" on its method name;
 */
public class UnitTestBuildHelper {

    private static Logger log = Logger.getLogger(UnitTestBuildHelper.class);

    private static final String REGEXP = "'su.*'"; //$NON-NLS-1$

    private static String[] filterExtensions = { "ana", "rep" };//$NON-NLS-1$//$NON-NLS-2$

    final private static String anaFolderName = "TDQ_Data Profiling/Analyses";//$NON-NLS-1$

    final private static String repFolderName = "TDQ_Data Profiling/Reports";//$NON-NLS-1$

    public static RepositoryNode mockRepositoryNode() {
        RepositoryNode node1 = mock(RepositoryNode.class);
        IRepositoryObject object = mock(IRepositoryObject.class);
        Property prop = mock(Property.class);
        Item item = mock(Item.class);
        ItemState state = mock(ItemState.class);
        when(prop.getItem()).thenReturn(item);
        when(node1.getObject()).thenReturn(object);
        when(object.getProperty()).thenReturn(prop);
        when(item.getState()).thenReturn(state);
        when(state.isDeleted()).thenReturn(false);
        return node1;
    }

    public static CorePlugin mockCorePlugin() {
        CorePlugin corePlugin = mock(CorePlugin.class);
        PowerMockito.mockStatic(CorePlugin.class);
        when(CorePlugin.getDefault()).thenReturn(corePlugin);
        return corePlugin;
    }

    public static ResourceBundle mockResourceBundle() {
        ResourceBundle rb = mock(ResourceBundle.class);
        stub(method(ResourceBundle.class, "getBundle", String.class)).toReturn(rb);
        return rb;
    }

    public static void mockMessages() {
        PowerMockito.mockStatic(Messages.class);
        when(Messages.getString(anyString())).thenReturn("a1");
        PowerMockito.mock(DefaultMessagesImpl.class);
        when(DefaultMessagesImpl.getString(anyString())).thenReturn("a2").thenReturn("a3").thenReturn("a4").thenReturn("a5");
    }

    public static Pattern createRealPattern() {
        Pattern pattern = PatternFactory.eINSTANCE.createPattern();
        pattern.setName("My Pattern"); //$NON-NLS-1$
        RegularExpression regularExpr = PatternFactory.eINSTANCE.createRegularExpression();
        TdExpression expression = createRealExpression();
        regularExpr.setExpression(expression);
        pattern.getComponents().add(regularExpr);
        return pattern;
    }

    public static TdExpression createRealExpression() {
        TdExpression expression = RelationalFactory.eINSTANCE.createTdExpression();
        expression.setBody(REGEXP);
        expression.setLanguage("SQL"); //$NON-NLS-1$
        return expression;
    }

    /**
     * create project with a specified name.
     * 
     * @param projectName specified project name
     * @return
     */
    public static IProject createRealProject(String projectName) {
        IProject rootProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!rootProject.exists()) {
            initProxyRepository(rootProject);
        }

        if (DQStructureManager.getInstance().isNeedCreateStructure()) {
            DQStructureManager.getInstance().createDQStructure();
        }
        return rootProject;
    }

    /**
     * DOC talend Comment method "initProxyRepository".
     */
    private static void initProxyRepository(IProject rootProject) {
        Project project = null;

        ProxyRepositoryFactory proxyRepository = ProxyRepositoryFactory.getInstance();
        IRepositoryFactory repository = RepositoryFactoryProvider.getRepositoriyById(RepositoryConstants.REPOSITORY_LOCAL_ID);
        if (repository == null) {
            log.fatal(DefaultMessagesImpl
                    .getString("No local Repository found! Probably due to a missing plugin in the product.")); //$NON-NLS-1$
        }
        proxyRepository.setRepositoryFactoryFromProvider(repository);
        try {
            proxyRepository.checkAvailability();
            proxyRepository.initialize();

            XmiResourceManager xmiResourceManager = new XmiResourceManager();

            if (rootProject.getFile(FileConstants.LOCAL_PROJECT_FILENAME).exists()) {
                // Initialize TDQ EMF model packages.
                new EMFUtil();
                project = new Project(xmiResourceManager.loadProject(rootProject));
            } else {
                User user = org.talend.core.model.properties.impl.PropertiesFactoryImpl.eINSTANCE.createUser();
                user.setLogin("talend@talend.com"); //$NON-NLS-1$
                user.setPassword("talend@talend.com".getBytes()); //$NON-NLS-1$
                String projectName = rootProject.getName();
                String projectDesc = ResourcesPlugin.getWorkspace().newProjectDescription(projectName).getComment();
                Project projectInfor = ProjectHelper.createProject(projectName, projectDesc, ECodeLanguage.JAVA.getName(), user);

                // MOD zshen create project by proxyRepository
                checkFileName(projectInfor.getLabel(), RepositoryConstants.PROJECT_PATTERN);

                project = proxyRepository.getRepositoryFactoryFromProvider().createProject(projectInfor);

            }

            if (project != null) {
                initRepositoryContext(project);

                // add status
                String defaultTechnicalStatusList = "DEV development;TEST testing;PROD production"; //$NON-NLS-1$
                List<Status> statusList = StatusHelper.parse(defaultTechnicalStatusList);
                proxyRepository.setTechnicalStatus(statusList);
            }

        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * DOC talend Comment method "createRealReport".
     * 
     * @param name the name of report
     * @param folder the path which report location
     * @param isDelete the report whether is logic delate
     */
    public static Report createRealReport(String name, IFolder folder, Boolean isDelete) {
        IPath createPath = Path.EMPTY;
        if (folder != null) {
            createPath = new Path(folder.getFullPath().lastSegment());
        }
        Report report1 = ReportHelper.createReport(name);
        TDQReportItem item1 = PropertiesFactoryImpl.eINSTANCE.createTDQReportItem();
        org.talend.core.model.properties.Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId(EcoreUtil.generateUUID());
        property1.setItem(item1);
        property1.setLabel(report1.getName());
        item1.setProperty(property1);
        item1.setReport(report1);
        ItemState itemState = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createItemState();
        itemState.setDeleted(isDelete);
        item1.setState(itemState);

        try {
            ProxyRepositoryFactory.getInstance().create(item1, createPath, false);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        return report1;
    }

    public static Analysis createRealAnalysis(String name, IFolder folder, Boolean isDelete) {
        IPath createPath = Path.EMPTY;
        if (folder != null) {
            createPath = new Path(folder.getFullPath().lastSegment());
        }
        Analysis analysis1 = AnalysisHelper.createAnalysis(name);
        TDQAnalysisItem item1 = PropertiesFactoryImpl.eINSTANCE.createTDQAnalysisItem();
        org.talend.core.model.properties.Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId(EcoreUtil.generateUUID());
        property1.setItem(item1);
        property1.setLabel(analysis1.getName());
        item1.setProperty(property1);
        item1.setAnalysis(analysis1);
        ItemState itemState = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createItemState();
        itemState.setDeleted(isDelete);
        item1.setState(itemState);
        AnalysisResult analysisResult1 = AnalysisFactory.eINSTANCE.createAnalysisResult();
        analysis1.setResults(analysisResult1);
        try {
            ProxyRepositoryFactory.getInstance().create(item1, createPath, false);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        return analysis1;
    }

    /**
     * DOC zshen Comment method "createFolder". create the subfolder under the parentFolder and named for folderName
     * 
     * @param parentFolder
     * @param folderName
     * @return
     */
    public static IFolder createRealFolder(IFolder parentFolder, String folderName) {
        IFolder currFolder = parentFolder.getFolder(folderName);
        if (!currFolder.exists()) {
            try {
                currFolder.create(true, true, null);
            } catch (CoreException e) {
                Assert.fail(e.getMessage());
            }
        }
        return currFolder;
    }

    /**
     * DOC zshen Comment method "checkFileName".
     * 
     * @param fileName
     * @param pattern
     * 
     * copy the method from ProxyRepositoryFactory to avoid tos migeration.
     */
    private static void checkFileName(String fileName, String pattern) {
        if (!java.util.regex.Pattern.matches(pattern, fileName)) {
            Assert.fail(DefaultMessagesImpl.getString("ProxyRepositoryFactory.illegalArgumentException.labelNotMatchPattern", //$NON-NLS-1$
                    new Object[] { fileName, pattern }));
            throw new IllegalArgumentException(DefaultMessagesImpl.getString(
                    "ProxyRepositoryFactory.illegalArgumentException.labelNotMatchPattern", new Object[] { fileName, pattern })); //$NON-NLS-1$
        }
    }

    private static void initRepositoryContext(Project project) {
        RepositoryContext repositoryContext = new RepositoryContext();
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        ctx.putProperty(Context.REPOSITORY_CONTEXT_KEY, repositoryContext);
        repositoryContext.setUser(project.getAuthor());
        repositoryContext.setClearPassword(project.getLabel());
        repositoryContext.setProject(project);
        repositoryContext.setFields(new HashMap<String, String>());
        //        repositoryContext.getFields().put(IProxyRepositoryFactory.BRANCH_SELECTION + "_" + project.getTechnicalLabel(), ""); //$NON-NLS-1$ //$NON-NLS-2$
        ProjectManager.getInstance().setMainProjectBranch(project, null);

        ReponsitoryContextBridge.initialized(project.getEmfProject(), project.getAuthor());
        // MOD zshen for bug tdq-4757 remove this init from corePlugin.start() to here because the initLocal command of
        // commandLine
        SQLExplorerPlugin.getDefault().setRootProject(ReponsitoryContextBridge.getRootProject());
    }

    /**
     * create the real RepositoryNode for DataProfiling.
     * 
     * @return
     */
    public static RepositoryNode createRealDataProfilingNode(IProject project) {
        RepositoryNode node = null;

        RepositoryNodeBuilder instance = RepositoryNodeBuilder.getInstance();
        FolderHelper folderHelper = instance.getFolderHelper();

        IFolder iFolder = project.getFolder(Path.fromPortableString(ERepositoryObjectType.TDQ_DATA_PROFILING.getFolder()));
        IRepositoryViewObject viewObject = null;
        if (folderHelper != null) {
            IPath relativePath = iFolder.getFullPath().makeRelativeTo((project).getFullPath());
            FolderItem folder2 = folderHelper.getFolder(relativePath);
            if (folder2 != null && relativePath != null) {
                viewObject = new Folder(folder2.getProperty(), instance.retrieveRepObjectTypeByPath(relativePath.toOSString()));
            }
        } else {
            viewObject = new Folder(iFolder.getName(), iFolder.getName());
        }
        node = new RepositoryNode(viewObject, null, ENodeType.SYSTEM_FOLDER);
        viewObject.setRepositoryNode(node);

        return node;
    }

    /**
     * create the real ReportFolderRepNode.
     * 
     * @param parentNode
     * @return
     */
    public static ReportFolderRepNode createRealReportFolderRepNode(RepositoryNode parentNode) {
        ReportFolderRepNode node = null;
        RepositoryNodeBuilder instance = RepositoryNodeBuilder.getInstance();
        try {
            node = (ReportFolderRepNode) instance.createRepositoryNodeSystemFolder(instance.getFolderHelper(), parentNode,
                    EResourceConstant.REPORTS);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        return node;
    }

    /**
     * create a real ReportSubFolderRepNode.
     * 
     * @param parentNode the parent node
     * @param folderName the folder name
     * @return
     */
    public static ReportSubFolderRepNode createRealReportSubFolderRepNode(RepositoryNode parentNode, String folderName) {
        ReportSubFolderRepNode folderNode = null;

        // create the sub folder
        IFolder iFolder = RepositoryNodeHelper.getIFolder(parentNode);
        createRealFolder(iFolder, folderName);

        // get the ReportSubFolderRepNode
        List<IRepositoryNode> subFolders = new ArrayList<IRepositoryNode>();
        if (parentNode instanceof ReportFolderRepNode) {
            subFolders = ((ReportFolderRepNode) parentNode).getChildren(false);
        } else if (parentNode instanceof ReportSubFolderRepNode) {
            subFolders = ((ReportSubFolderRepNode) parentNode).getChildren(false);
        }

        if (!subFolders.isEmpty()) {
            for (IRepositoryNode node : subFolders) {
                if (node instanceof ReportSubFolderRepNode) {
                    ReportSubFolderRepNode subNode = (ReportSubFolderRepNode) node;
                    if (folderName.equals(subNode.getLabel())) {
                        folderNode = subNode;
                        break;
                    }
                }
            }
        }

        return folderNode;
    }

    /**
     * create a real ReportRepNode.
     * 
     * @param name report name
     * @param folder report's parent folder
     * @param isDelete delete flag
     * @return
     */
    public static ReportRepNode createRealReportNode(String name, RepositoryNode parentNode, IPath createPath, Boolean isDelete) {
        ReportRepNode reportRepNode = null;

        Report report = ReportHelper.createReport(name);
        TDQReportItem reportItem = PropertiesFactoryImpl.eINSTANCE.createTDQReportItem();
        org.talend.core.model.properties.Property reportProperty = PropertiesFactory.eINSTANCE.createProperty();

        reportProperty.setId(EcoreUtil.generateUUID());
        reportProperty.setItem(reportItem);
        reportProperty.setLabel(report.getName());

        reportItem.setProperty(reportProperty);
        reportItem.setReport(report);

        ItemState itemState = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createItemState();
        itemState.setDeleted(isDelete);
        reportItem.setState(itemState);

        try {
            ProxyRepositoryFactory.getInstance().create(reportItem, createPath, false);

            IRepositoryViewObject reportViewObject = new RepositoryViewObject(reportProperty);
            reportRepNode = new ReportRepNode(reportViewObject, parentNode, ENodeType.REPOSITORY_ELEMENT);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }

        return reportRepNode;
    }

    /**
     * create the real SourceFileFolderRepNode.
     * 
     * @param parentNode
     * @return
     */
    public static SourceFileFolderRepNode createRealSourceFileFolderRepNode(RepositoryNode parentNode) {
        SourceFileFolderRepNode node = null;
        RepositoryNodeBuilder instance = RepositoryNodeBuilder.getInstance();
        try {
            node = (SourceFileFolderRepNode) instance.createRepositoryNodeSystemFolder(instance.getFolderHelper(), parentNode,
                    EResourceConstant.SOURCE_FILES);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }
        return node;
    }

    public static SourceFileSubFolderNode createRealSourceFileSubFolderRepNode(RepositoryNode parentNode, String folderName) {
        SourceFileSubFolderNode folderNode = null;

        // create the sub folder
        IFolder iFolder = RepositoryNodeHelper.getIFolder(parentNode);
        createRealFolder(iFolder, folderName);

        List<IRepositoryNode> subFolders = new ArrayList<IRepositoryNode>();
        if (parentNode instanceof SourceFileFolderRepNode) {
            subFolders = ((SourceFileFolderRepNode) parentNode).getChildren(false);
        } else if (parentNode instanceof SourceFileSubFolderNode) {
            subFolders = ((SourceFileSubFolderNode) parentNode).getChildren(false);
        }

        if (!subFolders.isEmpty()) {
            for (IRepositoryNode node : subFolders) {
                if (node instanceof SourceFileSubFolderNode) {
                    SourceFileSubFolderNode subNode = (SourceFileSubFolderNode) node;
                    if (folderName.equals(subNode.getLabel())) {
                        folderNode = subNode;
                        break;
                    }
                }
            }
        }

        return folderNode;
    }

    public static SourceFileRepNode createRealSourceFileNode(String name, RepositoryNode parentNode, IPath createPath,
            Boolean isDelete) {
        SourceFileRepNode fileRepNode = null;

        TDQSourceFileItem sourceFileItem = PropertiesFactoryImpl.eINSTANCE.createTDQSourceFileItem();
        org.talend.core.model.properties.Property fileProperty = PropertiesFactory.eINSTANCE.createProperty();

        fileProperty.setId(EcoreUtil.generateUUID());
        fileProperty.setItem(sourceFileItem);
        fileProperty.setLabel(name);

        sourceFileItem.setProperty(fileProperty);
        sourceFileItem.setFilename(name);
        sourceFileItem.setName(name);
        ByteArray byteArray = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createByteArray();
        byteArray.setInnerContent(PluginConstant.EMPTY_STRING.getBytes());
        sourceFileItem.setContent(byteArray);

        ItemState itemState = org.talend.core.model.properties.PropertiesFactory.eINSTANCE.createItemState();
        itemState.setDeleted(isDelete);
        sourceFileItem.setState(itemState);

        try {
            ProxyRepositoryFactory.getInstance().create(sourceFileItem, createPath, false);

            IRepositoryViewObject fileViewObject = new RepositoryViewObject(fileProperty);
            fileRepNode = new SourceFileRepNode(fileViewObject, parentNode, ENodeType.REPOSITORY_ELEMENT);
            fileRepNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.TDQ_SOURCE_FILE_ELEMENT);

            fileRepNode.setProperties(EProperties.LABEL, ERepositoryObjectType.TDQ_SOURCE_FILE_ELEMENT);
            fileViewObject.setRepositoryNode(fileRepNode);
        } catch (PersistenceException e) {
            Assert.fail(e.getMessage());
        }

        return fileRepNode;
    }

    /**
     * delete the project which has been login else will effect the result of junit.
     */
    public static void deleteCurrentProject() {
        IProject rootProject = ReponsitoryContextBridge.getRootProject();
        if (rootProject.exists()) {
            try {
                rootProject.delete(true, true, null);
            } catch (CoreException e) {
                log.error(e, e);
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     * delete the project which has been login else will effect the result of junit.
     */
    public static void deleteCurrentProject(String projectName) {
        IProject currProject = ReponsitoryContextBridge.findProject(projectName);
        if (currProject.exists()) {
            try {
                currProject.delete(true, true, null);
            } catch (CoreException e) {
                log.error(e, e);
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     * 
     * mock LocalRepositoryObjectCRUD for RepNodeUtils.getRepositoryObjectCRUD().
     */
    public static void mockLocalRepositoryObjectCRUD() {
        IProject proj = mock(IProject.class);
        when(proj.getFullPath()).thenReturn(new Path(PluginConstant.EMPTY_STRING));
        PowerMockito.mockStatic(ResourceManager.class);
        when(ResourceManager.getRootProject()).thenReturn(proj);

        PowerMockito.mockStatic(ProxyRepositoryFactory.class);
        ProxyRepositoryFactory proxFactory = mock(ProxyRepositoryFactory.class);
        when(ProxyRepositoryFactory.getInstance()).thenReturn(proxFactory);
        PowerMockito.mockStatic(RepNodeUtils.class);
        LocalRepositoryObjectCRUD localRepCRUD = mock(LocalRepositoryObjectCRUD.class);
        when(RepNodeUtils.getRepositoryObjectCRUD()).thenReturn(localRepCRUD);
    }
}
