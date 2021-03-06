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
package org.talend.dq.nodes;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.i18n.internal.DefaultMessagesImpl;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.FolderType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.dataquality.PluginConstant;
import org.talend.dq.helper.ProxyRepositoryManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;

/**
 * DOC klliu class global comment. Detailled comment
 */
public class RecycleBinRepNode extends DQRepositoryNode {

    private IImage icon;

    private String label;

    private static Logger log = Logger.getLogger(RecycleBinRepNode.class);

    /**
     * DOC klliu RecyleBinRepNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public RecycleBinRepNode(String label) {
        super(null, null, ENodeType.STABLE_SYSTEM_FOLDER);
        this.label = label;
        this.icon = ECoreImage.RECYCLE_BIN_EMPTY_ICON;
    }

    /**
     * Getter for icon.
     * 
     * @return the icon
     */
    @Override
    public IImage getIcon() {
        return this.icon;
    }

    /**
     * Sets the icon.
     * 
     * @param icon the icon to set
     */
    @Override
    public void setIcon(IImage icon) {
        this.icon = icon;
    }

    /**
     * Getter for label.
     * 
     * @return the label
     */
    @Override
    public String getLabel() {
        if (this.label == null) {
            this.label = DefaultMessagesImpl.getString("RecycleBin.resBinName"); //$NON-NLS-1$
        }
        return this.label;
    }

    /**
     * Sets the label.
     * 
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean isBin() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.RepositoryNode#getChildren()
     */
    @Override
    public List<IRepositoryNode> getChildren() {
        super.getChildren().clear();
        List<DQRepositoryNode> foldersList = new ArrayList<DQRepositoryNode>();
        Project newProject = ProjectManager.getInstance().getCurrentProject();
        List<FolderItem> folderItems = ProjectManager.getInstance().getFolders(newProject.getEmfProject());
        for (FolderItem folderItem : folderItems) {
            if (isTDQOrMetadataRootFolder(folderItem)) {
                addItemToRecycleBin(this, folderItem, foldersList);
            }
        }
        return filterRecycleBin(super.getChildren());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.RepositoryNode#getChildren(boolean)
     */
    @Override
    public List<IRepositoryNode> getChildren(boolean withDeleted) {
        // override this method just used to get super getChildren method
        return super.getChildren();
    }

    public boolean isTDQOrMetadataRootFolder(FolderItem folderItem) {

        String path = getFullFolderPath(folderItem, PluginConstant.EMPTY_STRING);
        if (path != null && (path.startsWith("TDQ") || path.startsWith("metadata"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return true;
        }
        return false;
    }

    private String getFullFolderPath(FolderItem folder, String path) {
        if (folder.getParent() instanceof FolderItem) {
            return getFullFolderPath((FolderItem) folder.getParent(), folder.getProperty().getLabel() + "/" + path);//$NON-NLS-1$
        }
        return folder.getProperty().getLabel() + "/" + path;//$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    private void addItemToRecycleBin(DQRepositoryNode parentNode, Item item, List<DQRepositoryNode> foldersList) {
        ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
        DQRepositoryNode currentParentNode = parentNode;
        if (item instanceof FolderItem) {
            itemType = getFolderContentType((FolderItem) item);
            EList<Item> childrenList = ((FolderItem) item).getChildren();
            if (item.getState() != null && item.getState().isDeleted()) {
                // need to display this folder in the recycle bin.
                Folder folder = new Folder(item.getProperty(), itemType);
                DQRepositoryNode folderNode = null;
                for (DQRepositoryNode existingFolder : foldersList) {
                    if (existingFolder.getContentType() == null) {
                        // this can appear temporary when another user has deleted a folder
                        break;
                    }
                    if (existingFolder.getContentType().equals(folder.getRepositoryObjectType())
                            && existingFolder.getProperties(EProperties.LABEL).equals(folder.getLabel())) {
                        folderNode = existingFolder;
                        break;
                    }
                }
                if (folderNode == null) {
                    folderNode = new DQRepositoryNode(folder, parentNode, ENodeType.SIMPLE_FOLDER);
                    folderNode.setProperties(EProperties.CONTENT_TYPE, itemType);
                    folderNode.setProperties(EProperties.LABEL, folder.getLabel());
                    foldersList.add(folderNode);
                    parentNode.getChildren(false).add(folderNode);
                    folderNode.setParent(parentNode);
                }
                if (childrenList.isEmpty()) {
                    initChildForRemoteProject((FolderItem) item, itemType);
                }
                for (Item curItem : new ArrayList<Item>(((FolderItem) item).getChildren())) {
                    addItemToRecycleBin(folderNode, curItem, foldersList);
                }
                currentParentNode = folderNode;
            } else {
                // TDQ-6184,When user A delete an item from FolderItem,user B should initialize(get) all children from
                // the FolderItem in some cases.
                if (childrenList.isEmpty()) {
                    initChildForRemoteProject((FolderItem) item, itemType);
                }
                for (Item curItem : new ArrayList<Item>(((FolderItem) item).getChildren())) {
                    addItemToRecycleBin(parentNode, curItem, foldersList);
                }
            }
        } else if (item.getState() != null && item.getState().isDeleted()) {
            try {
                if (item.getProperty().getVersion()
                        .equals(ProxyRepositoryFactory.getInstance().getLastVersion(item.getProperty().getId()).getVersion())) {
                    DQRepositoryNode repNode = new DQRepositoryNode(new RepositoryViewObject(item.getProperty()),
                            currentParentNode, ENodeType.REPOSITORY_ELEMENT);
                    repNode.setProperties(EProperties.CONTENT_TYPE, itemType);
                    repNode.setProperties(EProperties.LABEL, item.getProperty().getLabel());
                    currentParentNode.getChildren(false).add(repNode);
                    repNode.setParent(currentParentNode);
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private ERepositoryObjectType getFolderContentType(FolderItem folderItem) {
        if (!folderItem.getType().equals(FolderType.SYSTEM_FOLDER_LITERAL)) {
            if (!(folderItem.getParent() instanceof FolderItem)) {
                return null; // appears only for a folder for expression builder !
            }
            return getFolderContentType((FolderItem) folderItem.getParent());
        }
        for (ERepositoryObjectType objectType : (ERepositoryObjectType[]) ERepositoryObjectType.values()) {
            String folderName;
            try {
                folderName = ERepositoryObjectType.getFolderName(objectType);
            } catch (Exception e) {
                // just catch exception to avoid the types who don't have folders
                continue;
            }
            if (folderName.contains("/")) { //$NON-NLS-1$
                String[] folders = folderName.split("/"); //$NON-NLS-1$
                FolderItem currentFolderItem = folderItem;
                boolean found = true;
                for (int i = folders.length - 1; i >= 0; i--) {
                    if (!currentFolderItem.getProperty().getLabel().equals(folders[i])) {
                        found = false;
                        break;
                    }
                    if (i > 0) {
                        if (!(currentFolderItem.getParent() instanceof FolderItem)) {
                            found = false;
                            break;
                        }
                        currentFolderItem = (FolderItem) currentFolderItem.getParent();
                    }
                }
                if (found) {
                    return objectType;
                }
            } else {
                if (folderName.equals(folderItem.getProperty().getLabel())) {
                    return objectType;
                }
            }
        }
        if (folderItem.getParent() instanceof FolderItem) {
            return getFolderContentType((FolderItem) folderItem.getParent());
        }
        return null;
    }

    /**
     * 
     * init the children for remote FolderItem.
     * 
     * @param folderItem
     * @param itemType
     */
    private void initChildForRemoteProject(FolderItem folderItem, ERepositoryObjectType itemType) {
        boolean isLocalProject = ProxyRepositoryManager.getInstance().isLocalProject();
        if (!isLocalProject && folderItem.getChildren().isEmpty()) {
            try {
                String path = getFullFolderPath(folderItem, PluginConstant.EMPTY_STRING);
                ProxyRepositoryFactory.getInstance().getTdqRepositoryViewObjects(itemType, path);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }

}
