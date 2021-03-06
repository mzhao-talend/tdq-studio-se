package net.sourceforge.sqlexplorer.dbproduct;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLCannotConnectException;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.id.IIdentifier;
import net.sourceforge.squirrel_sql.fw.persist.ValidationException;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;
import net.sourceforge.squirrel_sql.fw.util.beanwrapper.StringWrapper;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.HotClassLoader;
import org.talend.core.model.metadata.builder.database.JDBCDriverLoader;
import org.talend.core.model.metadata.builder.database.PluginConstant;
import org.talend.metadata.managment.hive.HiveClassLoaderFactory;
import org.talend.utils.sql.ConnectionUtils;

/**
 * Manages a JDBC Driver
 * 
 * @author John Spackman
 */
public class ManagedDriver implements Comparable<ManagedDriver> {

    public class SQLDriver implements ISQLDriver {

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void assignFrom(ISQLDriver rhs) throws ValidationException {
            throw new ValidationException(Messages.getString("ManagedDriver.NotSupported")); //$NON-NLS-1$
        }

        public int compareTo(ISQLDriver rhs) {
            return ManagedDriver.this.getDriverClassName().compareTo(rhs.getDriverClassName());
        }

        public String getDriverClassName() {
            return ManagedDriver.this.getDriverClassName();
        }

        public IIdentifier getIdentifier() {
            return null;
        }

        public String getJarFileName() {
            return null;
        }

        public String[] getJarFileNames() {
            return (String[]) ManagedDriver.this.getJars().toArray();
        }

        public StringWrapper getJarFileNameWrapper(int idx) throws ArrayIndexOutOfBoundsException {
            return null;
        }

        public StringWrapper[] getJarFileNameWrappers() {
            return null;
        }

        public String getName() {
            return ManagedDriver.this.getDriverClassName();
        }

        public String getUrl() {
            return ManagedDriver.this.getUrl();
        }

        public String getWebSiteUrl() {
            return null;
        }

        public boolean isJDBCDriverClassLoaded() {
            return ManagedDriver.this.isDriverClassLoaded();
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public void setDriverClassName(String driverClassName) throws ValidationException {
        }

        public void setJarFileName(String value) throws ValidationException {
        }

        public void setJarFileNames(String[] values) {
        }

        public void setJarFileNameWrapper(int idx, StringWrapper value) throws ArrayIndexOutOfBoundsException {
        }

        public void setJarFileNameWrappers(StringWrapper[] value) {
        }

        public void setJDBCDriverClassLoaded(boolean cl) {
        }

        public void setName(String name) throws ValidationException {
        }

        public void setUrl(String url) throws ValidationException {
        }

        public void setWebSiteUrl(String url) throws ValidationException {
        }
    }

    private String id;

    private String name;

    private String driverClassName;

    private String url;

    private LinkedList<String> jars = new LinkedList<String>();

    private Driver jdbcDriver;

    private static Logger log = Logger.getLogger(ManagedDriver.class);

    public ManagedDriver(String id) {
        this.id = id;
    }

    /**
     * Constructs a new ManagedDriver from a previously serialised version
     * 
     * @param root result of previous call to describeAsXml()
     */
    public ManagedDriver(Element root) {
        super();
        id = root.attributeValue(DriverManager.ID);
        name = root.elementText(DriverManager.NAME);
        driverClassName = root.elementText(DriverManager.DRIVER_CLASS);
        url = root.elementText(DriverManager.URL);
        Element jarsElem = root.element(DriverManager.JARS);
        List<Element> list = jarsElem.elements();
        if (list != null) {
            for (Element jarElem : list) {
                String jar = jarElem.getTextTrim();
                if (jar != null) {
                    jars.add(jar);
                }
            }
        }
    }

    /**
     * Describes this driver in XML; the result can be passed to the constructor to refabricate it late
     * 
     * @return
     */
    public Element describeAsXml() {
        Element root = new DefaultElement(DriverManager.DRIVER);
        root.addAttribute(DriverManager.ID, id);
        root.addElement(DriverManager.NAME).setText(name);
        if (driverClassName != null) {
            root.addElement(DriverManager.DRIVER_CLASS).setText(driverClassName);
        }
        root.addElement(DriverManager.URL).setText(url);
        Element jarsElem = root.addElement(DriverManager.JARS);
        for (String jar : jars) {
            jarsElem.addElement(DriverManager.JAR).setText(jar);
        }
        return root;
    }

    /**
     * Loads the Driver class
     * 
     * @throws ExplorerException
     * @throws SQLException
     * @deprecated replace it with registerSQLDriver(String dbType, String dbVersion)
     */
    @Deprecated
    public synchronized void registerSQLDriver() throws ClassNotFoundException {
        if (driverClassName == null || driverClassName.length() == 0) {
            return;
        }
        unregisterSQLDriver();
        jdbcDriver = DatabaseProductFactory.loadDriver(this);
    }

    /**
     * regist jdbc driver except Hive by HotClassLoader.
     * 
     * @param dbConnection
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void registerSQLDriver(String dbType, String dbVersion) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        boolean isODBC = dbType.equalsIgnoreCase(EDatabaseTypeName.GODBC.getXmlName());
        if (driverClassName != null && !dbType.equalsIgnoreCase(EDatabaseTypeName.HIVE.getXmlName())
                && (isODBC || isValidatedJars())) {

            unregisterSQLDriver();
            JDBCDriverLoader jdbcDriverClassLoader = new JDBCDriverLoader();
            HotClassLoader hotClassLoader = jdbcDriverClassLoader.getHotClassLoader(jars.toArray(new String[jars.size()]),
                    dbType, dbVersion);
            Class<?> classDriver = Class.forName(driverClassName, true, hotClassLoader);
            if (classDriver != null) {
                jdbcDriver = (Driver) classDriver.newInstance();
            }
        }
        if (jdbcDriver == null) {
            log.error("fail to regist jdbc driver in SQLExplorer");
        }

    }

    /**
     * 
     * register hive jdbc driver by DatabaseConnection
     * 
     * @param dbConnection
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    public synchronized void registerHiveSQLDriver(DatabaseConnection dbConnection) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        if (dbConnection != null || driverClassName != null || driverClassName.length() != 0) {
            String dbType = dbConnection.getDatabaseType();
            if (dbType.equalsIgnoreCase(EDatabaseTypeName.HIVE.getXmlName())) {
                unregisterSQLDriver();
                IMetadataConnection metadataConn = ConvertionHelper.convert(dbConnection);
                ClassLoader classLoader = HiveClassLoaderFactory.getInstance().getClassLoader(metadataConn);
                Class<?> classDriver = Class.forName(driverClassName, true, classLoader);
                jdbcDriver = (Driver) classDriver.newInstance();
            }
        }
        if (jdbcDriver == null) {
            log.error("fail to regist Hive jdbc driver in SQLExplorer");
        }

    }

    /**
     * Unloads the class
     * 
     */
    public synchronized void unregisterSQLDriver() {
        jdbcDriver = null;
    }

    /**
     * Establishes a JDBC connection
     * 
     * @param user
     * @return
     * @throws ExplorerException
     * @throws SQLException
     */
    public SQLConnection getConnection(User user) throws SQLException {
        Properties props = new Properties();
        // MOD msjian TDQ-8463: for the string "user" and "password", no need to do international.bacause some
        // jars(e.g:ojdbc14.jar) don't support international and cause to get connection error
        if (user.getUserName() != null) {
            props.put("user", user.getUserName()); //$NON-NLS-1$
        }
        if (user.getPassword() != null) {
            props.put("password", user.getPassword());//$NON-NLS-1$
        }
        if (!isDriverClassLoaded()) {
            try {
                DatabaseConnection dbConn = user.getDatabaseConnection();
                if (dbConn != null) {
                    registerSQLDriver(dbConn.getDatabaseType(), dbConn.getDbVersionString());
                }
            } catch (Exception e) {
                throw new SQLException(Messages.getString("ManagedDriver.CannotLoadDriver1", driverClassName) + " "//$NON-NLS-1$ //$NON-NLS-2$
                        + Messages.getString("ManagedDriver.CannotLoadDriver2"));//$NON-NLS-1$
            }
        }
        if (!isDriverClassLoaded()) {
            throw new SQLException(Messages.getString("ManagedDriver.CannotLoadDriver1", driverClassName));//$NON-NLS-1$
        }

        Connection jdbcConn = null;
        try {
            String dbUrl = user.getAlias().getUrl();
            if (ConnectionUtils.isHsql(dbUrl)) {
                dbUrl = ConnectionUtils.addShutDownForHSQLUrl(dbUrl, user.getDatabaseConnection().getAdditionalParams());
            }
            jdbcConn = jdbcDriver.connect(dbUrl, props);
        } catch (SQLException e) {
            throw new SQLCannotConnectException(user, e);
        }
        if (jdbcConn == null) {
            throw new SQLCannotConnectException(user);
        }

        return new SQLConnection(user, jdbcConn, this, getDatabaseProduct().describeConnection(jdbcConn));
    }

    public boolean isDriverClassLoaded() {
        return jdbcDriver != null;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getId() {
        return id;
    }

    public LinkedList<String> getJars() {
        return jars;
    }

    public Driver getJdbcDriver() {
        return jdbcDriver;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setJars(LinkedList<String> jars) {
        this.jars = jars;
    }

    public void setJars(String[] jars) {
        this.jars.clear();
        for (String jar : jars) {
            this.jars.add(jar);
        }
    }

    public void setJdbcDriver(Driver jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public DatabaseProduct getDatabaseProduct() {
        return DatabaseProductFactory.getInstance(this);
    }

    public int compareTo(ManagedDriver that) {
        return name.compareTo(that.name);
    }

    public boolean isUsedByAliases() {
        Collection<Alias> aliases = SQLExplorerPlugin.getDefault().getAliasManager().getAliases();
        for (Alias alias : aliases) {
            if (alias.getDriverId().equals(this.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidatedJars() {
        boolean flag = false;
        if (jars.isEmpty()) {
            return false;
        }
        for (String jarPath : jars) {
            if (jarPath == null || jarPath.equals(PluginConstant.EMPTY_STRING)) {
                return false;
            }
            File jarFile = new File(jarPath);
            flag = jarFile.exists() && jarFile.isFile();
        }
        return true;
    }
}
