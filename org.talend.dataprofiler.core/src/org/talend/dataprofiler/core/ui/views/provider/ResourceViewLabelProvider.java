// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.views.provider;

import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.cwm.softwaredeployment.TdDataProvider;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.manager.DQStructureManager;
import org.talend.dataprofiler.core.ui.action.provider.NewSourcePatternActionProvider;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.domain.pattern.Pattern;
import org.talend.dataquality.reports.TdReport;
import org.talend.dataquality.rules.WhereRule;
import org.talend.dataquality.utils.DateFormatUtils;
import org.talend.dq.helper.resourcehelper.AnaResourceFileHelper;
import org.talend.dq.helper.resourcehelper.DQRuleResourceFileHelper;
import org.talend.dq.helper.resourcehelper.PatternResourceFileHelper;
import org.talend.dq.helper.resourcehelper.PrvResourceFileHelper;
import org.talend.dq.helper.resourcehelper.RepResourceFileHelper;
import org.talend.utils.sugars.TypedReturnCode;

/**
 * @author rli
 * 
 */
public class ResourceViewLabelProvider extends WorkbenchLabelProvider implements ICommonLabelProvider {

    private static Logger log = Logger.getLogger(ResourceViewLabelProvider.class);

    public void init(ICommonContentExtensionSite aConfig) {
    }

    protected ImageDescriptor decorateImage(ImageDescriptor input, Object element) {
        if (element instanceof IFile) {
            IFile file = (IFile) element;
            if (file.getFileExtension().equalsIgnoreCase(FactoriesUtil.PATTERN)) {
                Pattern findPattern = PatternResourceFileHelper.getInstance().findPattern(file);
                ImageDescriptor imageDescriptor = ImageLib.getImageDescriptor(ImageLib.PATTERN_REG);
                if (findPattern != null) {
                    boolean validStatus = TaggedValueHelper.getValidStatus(findPattern);
                    if (!validStatus) {
                        ImageDescriptor warnImg = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                                ISharedImages.IMG_OBJS_WARN_TSK);
                        DecorationOverlayIcon icon = new DecorationOverlayIcon(imageDescriptor.createImage(), warnImg,
                                IDecoration.BOTTOM_RIGHT);
                        imageDescriptor = icon;
                    }
                }
                return imageDescriptor;
            } else if (file.getFileExtension().equalsIgnoreCase(FactoriesUtil.REP)) {
                return ImageLib.getImageDescriptor(ImageLib.REPORT_OBJECT);
            }
        }
        if (element instanceof IProject) {
            if (DQStructureManager.METADATA.equals(((IProject) element).getName())) {
                return ImageLib.getImageDescriptor(ImageLib.METADATA);
            } else if (DQStructureManager.LIBRARIES.equals(((IProject) element).getName())) {
                return ImageLib.getImageDescriptor(ImageLib.LIBRARIES);
            } else if (DQStructureManager.DATA_PROFILING.equals(((IProject) element).getName())) {
                return ImageLib.getImageDescriptor(ImageLib.DATA_PROFILING);
            }
        } else if (element instanceof IFolder) {
            if (DQStructureManager.DB_CONNECTIONS.equals(((IFolder) element).getName())) {
                return ImageLib.getImageDescriptor(ImageLib.CONNECTION);
            }
        }
        return super.decorateImage(input, element);
    }

    public String getDescription(Object anElement) {

        if (anElement instanceof IResource) {
            return ((IResource) anElement).getFullPath().makeRelative().toString();
        }
        return null;
    }

    public void restoreState(IMemento aMemento) {

    }

    public void saveState(IMemento aMemento) {
    }

    protected String decorateText(String input, Object element) {
        if (input.endsWith(org.talend.dq.PluginConstant.PRV_SUFFIX)) {
            IFile fileElement = (IFile) element;
            TypedReturnCode<TdDataProvider> rc = PrvResourceFileHelper.getInstance().findProvider(fileElement);
            String decorateText = PluginConstant.EMPTY_STRING;
            if (rc.isOk()) {
                decorateText = rc.getObject().getName();
            } else {
                log.warn(rc.getMessage());
            }
            return decorateText;
        } else if (input.endsWith(org.talend.dq.PluginConstant.ANA_SUFFIX)) {
            IFile fileElement = (IFile) element;
            if (log.isDebugEnabled()) {
                log.debug("Loading file " + (fileElement).getLocation()); //$NON-NLS-1$
            }
            Analysis analysis = AnaResourceFileHelper.getInstance().findAnalysis(fileElement);
            if (analysis != null) {
                Date executionDate = analysis.getResults().getResultMetadata().getExecutionDate();
                String executeInfo = executionDate == null ? DefaultMessagesImpl.getString("ResourceViewLabelProvider.executed") : PluginConstant.PARENTHESIS_LEFT //$NON-NLS-1$
                                + DateFormatUtils.getSimpleDateString(executionDate) + PluginConstant.PARENTHESIS_RIGHT;
                return analysis.getName() + PluginConstant.SPACE_STRING + executeInfo;
            }
        } else if (input.endsWith(NewSourcePatternActionProvider.EXTENSION_PATTERN)) {
            IFile file = (IFile) element;
            Pattern pattern = PatternResourceFileHelper.getInstance().findPattern(file);
            return pattern.getName();
        } else if (input.endsWith(org.talend.dq.PluginConstant.REP_SUFFIX)) {
            IFile fileElement = (IFile) element;
            TdReport findReport = RepResourceFileHelper.getInstance().findReport(fileElement);
            return findReport.getName();
        } else if (input.endsWith(FactoriesUtil.DQRULE)) {
            IFile file = (IFile) element;
            WhereRule wr = DQRuleResourceFileHelper.getInstance().findWhereRule(file);
            return wr.getName();
        }
        return super.decorateText(input, element);
    }
}
