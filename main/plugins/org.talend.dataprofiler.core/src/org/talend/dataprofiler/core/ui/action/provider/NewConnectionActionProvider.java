// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.action.provider;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.talend.dataprofiler.core.service.AbstractSvnRepositoryService;
import org.talend.dataprofiler.core.service.GlobalServiceRegister;
import org.talend.dataprofiler.core.ui.action.actions.CreateDBConnectionAction;
import org.talend.dataprofiler.core.ui.action.actions.CreateDFConnectionAction;
import org.talend.dataprofiler.core.ui.action.actions.CreateMDMConnectionAction;
import org.talend.dq.nodes.DBConnectionFolderRepNode;
import org.talend.dq.nodes.DFConnectionFolderRepNode;
import org.talend.dq.nodes.DFConnectionSubFolderRepNode;
import org.talend.dq.nodes.MDMConnectionFolderRepNode;
import org.talend.dq.nodes.MDMConnectionSubFolderRepNode;
import org.talend.repository.model.RepositoryNode;

/**
 * @author rli
 * 
 */
public class NewConnectionActionProvider extends AbstractCommonActionProvider {

    protected static Logger log = Logger.getLogger(NewConnectionActionProvider.class);

    /**
     * 
     */
    public NewConnectionActionProvider() {
    }

    /**
     * Adds a submenu to the given menu with the name "New Component".
     */
    public void fillContextMenu(IMenuManager menu) {
        // MOD mzhao user readonly role on svn repository mode.
        if (!isShowMenu()) {
            return;
        }
        // MOD mzhao user readonly role on svn repository mode.
        AbstractSvnRepositoryService svnReposService = GlobalServiceRegister.getDefault().getSvnRepositoryService(
                AbstractSvnRepositoryService.class);
        if (svnReposService != null && svnReposService.isReadonly()) {
            return;
        }

		//MOD gdbu 2011-4-1 bug 20051
        RepositoryNode node = (RepositoryNode) getFistContextNode();

        if (node != null) {
		//~20051
            IAction action = null;
            if (node instanceof DBConnectionFolderRepNode) {
                action = new CreateDBConnectionAction(node);
            } else if (node instanceof DFConnectionFolderRepNode || node instanceof DFConnectionSubFolderRepNode) {
                action = new CreateDFConnectionAction(node);
            } else if (node instanceof MDMConnectionFolderRepNode || node instanceof MDMConnectionSubFolderRepNode) {
                // MOD klliu 20744: there is a lack of "Create MDM Connection" menu 2011-04-19
                action = new CreateMDMConnectionAction(node);
            }

            if (action != null) {
                menu.add(action);
            }

        }
    }
}
