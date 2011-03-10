// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
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

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.repositoryObject.MetadataCatalogRepositoryObject;
import org.talend.core.repository.model.repositoryObject.MetadataSchemaRepositoryObject;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.Schema;

/**
 * DOC klliu Database connection repository node displayed on repository view (UI).
 */
public class DBConnectionRepNode extends ConnectionRepNode {

    private DatabaseConnection databaseConnection;

    public DatabaseConnection getDatabaseConnection() {
        return this.databaseConnection;
    }

    /**
     * DOC klliu DBConnectionRepNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public DBConnectionRepNode(IRepositoryViewObject object, RepositoryNode parent, ENodeType type) {
        super(object, parent, type);
        if (object != null && object.getProperty() != null) {
            Item item = object.getProperty().getItem();
            if (item != null && item instanceof DatabaseConnectionItem) {
                this.databaseConnection = (DatabaseConnection) ((DatabaseConnectionItem) item).getConnection();
            }
        }
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.talend.repository.model.RepositoryNode#getChildren()
     */
    @Override
    public List<IRepositoryNode> getChildren() {
        // Retrieve catalogs/schemes.
        Connection connection = ((ConnectionItem) getObject().getProperty().getItem()).getConnection();
        EList<Package> dataPackage = connection.getDataPackage();
        if (dataPackage != null && dataPackage.size() > 0) {
            Package pack = dataPackage.get(0);
            if (pack instanceof Schema) {
                return createRepositoryNodeSchema(dataPackage);
            } else if (pack instanceof Catalog) {
                return createRepositoryNodeCatalog(dataPackage);
            }
        }
        return new ArrayList<IRepositoryNode>();

    }

    /**
     * DOC klliu Comment method "createRepositoryNodeSchema".
     * 
     * @param node
     * @param viewObject
     * @param schema
     */
    private List<IRepositoryNode> createRepositoryNodeSchema(EList<Package> dataPackage) {
        List<IRepositoryNode> nodes = new ArrayList<IRepositoryNode>();
        // EList<Package> dataPackage= ((ConnectionItem)
        // getObject().getProperty().getItem()).getConnection().getDataPackage();
        for (Package pack : dataPackage) {
            MetadataSchemaRepositoryObject metadataSchema = new MetadataSchemaRepositoryObject(getObject(), (Schema) pack);
            RepositoryNode schemaNode = new DBSchemaRepNode((IRepositoryViewObject) metadataSchema, this,
                    ENodeType.TDQ_REPOSITORY_ELEMENT);
            schemaNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_CON_SCHEMA);
            schemaNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CON_SCHEMA);
            metadataSchema.setRepositoryNode(schemaNode);
            nodes.add(schemaNode);
        }
        return nodes;
    }

    /**
     * DOC klliu Comment method "createRepositoryNodeCatalog".
     * 
     * @param node
     * @param viewObject
     * @param catalog
     */
    private List<IRepositoryNode> createRepositoryNodeCatalog(EList<Package> dataPackage) {
        List<IRepositoryNode> nodes = new ArrayList<IRepositoryNode>();
        // EList<Package> dataPackage = ((ConnectionItem)
        // getObject().getProperty().getItem()).getConnection().getDataPackage();
        for (Package pack : dataPackage) {
            MetadataCatalogRepositoryObject metadataCatalog = new MetadataCatalogRepositoryObject(getObject(), (Catalog) pack);
            RepositoryNode catalogNode = new DBCatalogRepNode((IRepositoryViewObject) metadataCatalog, this,
                    ENodeType.TDQ_REPOSITORY_ELEMENT);
            catalogNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_CON_CATALOG);
            catalogNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CON_CATALOG);
            metadataCatalog.setRepositoryNode(catalogNode);
            nodes.add(catalogNode);
        }
        return nodes;
    }

    @Override
    public String getLabel() {
        if (this.getDatabaseConnection() != null) {
            return this.getDatabaseConnection().getName();
        }
        return super.getLabel();
    }

    @Override
    public boolean canExpandForDoubleClick() {
        return false;
    }
}
