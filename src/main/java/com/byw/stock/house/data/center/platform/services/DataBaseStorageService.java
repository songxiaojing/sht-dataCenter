package com.byw.stock.house.data.center.platform.services;

import com.byw.stock.house.platform.core.exception.PlatformException;
import com.byw.stock.house.platform.core.services.IPlatformService;
import com.byw.stock.house.platform.core.services.PlatformServiceInfo;
import com.byw.stock.house.platform.core.utils.Assert;
import com.byw.stock.house.platform.log.PlatformLogger;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.xml.bind.annotation.XmlElement;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 数据库存储服务.
 * <p>
 * 用于服务层的数据库操作.
 *
 * @author baiyanwei
 * @title DataBaseStorageService
 * @package com.topsec.tss.platform.service
 * @date 2014-10-29
 */
@PlatformServiceInfo(description = "The DataBase Storage Service in Platform.", configurationPath = "application/services/DataBaseStorageService/")
public class DataBaseStorageService implements IPlatformService {

    /**
     * logger
     */
    final private static PlatformLogger theLogger = PlatformLogger.getLogger(DataBaseStorageService.class);
    // c3p0.driverClass 数据库连接驱动类
    @XmlElement(name = "driverClass", defaultValue = "")
    public String _driverClass = "";
    // c3p0.jdbcUrl 数据库连接URL
    @XmlElement(name = "jdbcUrl", defaultValue = "")
    public String _jdbcUrl = "";
    // c3p0.user 数据库用户名
    @XmlElement(name = "user", defaultValue = "")
    public String _user = "";
    // c3p0.password 数据库密码
    @XmlElement(name = "password", defaultValue = "")
    public String _password = "";
    // c3p0.initialPoolSize 连接池初始值
    @XmlElement(name = "initialPoolSize", type = Integer.class, defaultValue = "20")
    public Integer _initialPoolSize = new Integer(20);
    // c3p0.maxIdleTime 最大空闲时间
    @XmlElement(name = "maxIdleTime", type = Integer.class, defaultValue = "5")
    public Integer _maxIdleTime = new Integer(5);
    // c3p0.maxPoolSize 连接池最大值
    @XmlElement(name = "maxPoolSize", type = Integer.class, defaultValue = "20")
    public Integer _maxPoolSize = new Integer(20);
    // c3p0.minPoolSize 连接池最小值 
    @XmlElement(name = "minPoolSize", type = Integer.class, defaultValue = "5")
    public Integer _minPoolSize = new Integer(5);

    // DataBase pool instance 数据库连接池
    private ComboPooledDataSource _cpds = null;

    @Override
    public void start() throws PlatformException {

        long currentPoint = System.currentTimeMillis();
        //创建连接池
        createDataBaseConnectionPool();

        theLogger.info("startUp", _jdbcUrl, String.valueOf(System.currentTimeMillis() - currentPoint));
    }

    @Override
    public void stop() throws PlatformException {

        stopeDataBaseConnectionPool();
        _cpds = null;
    }

    /**
     * 创建连接池.
     */
    private void createDataBaseConnectionPool() {

        _cpds = new ComboPooledDataSource();
        try {
            _cpds.setDriverClass(this._driverClass);
            _cpds.setJdbcUrl(this._jdbcUrl);
            _cpds.setUser(this._user);
            _cpds.setPassword(this._password);
            _cpds.setInitialPoolSize(this._initialPoolSize);
            _cpds.setMaxIdleTime(this._maxIdleTime);
            _cpds.setMaxPoolSize(this._maxPoolSize);
            _cpds.setMinPoolSize(this._minPoolSize);
            theLogger.info("databaseParameter", _driverClass, _jdbcUrl, _user, _password, String.valueOf(_initialPoolSize), String.valueOf(_maxIdleTime),
                    String.valueOf(_maxPoolSize), String.valueOf(_minPoolSize));
        } catch (Exception e) {
            theLogger.exception(e);
        }
    }

    /**
     * 销毁连接池
     */
    private void stopeDataBaseConnectionPool() {

        if (_cpds != null) {
            _cpds.close();
        }
    }

    /**
     * @return
     * @throws SQLException Get a DataBase connection. must close the connection when done.
     */
    public Connection getConnection() throws SQLException {

        return _cpds.getConnection();
    }

    /**
     * 批量插入.
     *
     * @param sql
     * @param conn
     * @param beanList
     * @param paramsField
     * @throws Exception
     */
    public void bulidBatchInsert(String sql, Connection conn, List<Map<String, String>> beanList, String[] paramsField) throws Exception {

        //验证参数
        if (Assert.isEmptyString(sql) == true || conn == null || Assert.isEmptyCollection(beanList) == true || paramsField == null || paramsField.length <= 0) {
            throw new Exception("#批量插入参数不足");
        }
        PreparedStatement pstmt = null;
        try {
            conn.setAutoCommit(false);//手动提交     
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < beanList.size(); i++) {
                for (int j = 0; j < paramsField.length; j++) {
                    pstmt.setObject(j + 1, beanList.get(i).get(paramsField[j]));
                }
                pstmt.addBatch();
                //1000条执行一次
                if (i != 0 && i % 1000 == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    pstmt.clearBatch();
                }
            }
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
            theLogger.exception("sql:" + sql, e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 取得关系数据.
     *
     * @param sql
     * @param conn
     * @return
     */
    public List<Long[]> bulidRelationIdsList(String sql, Connection conn) {

        if (Assert.isEmptyString(sql) == true) {
            return null;
        }
        System.out.println(sql);
        theLogger.debug("sql", sql);
        //
        Statement statment = null;
        ResultSet rs = null;
        List<Long[]> resultList = new ArrayList<Long[]>();
        try {
            statment = conn.createStatement();
            rs = statment.executeQuery(sql);
            while (rs.next()) {
                resultList.add(new Long[]{rs.getLong(1), rs.getLong(2)});
            }
        } catch (Exception e) {
            theLogger.exception("sql:" + sql, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (statment != null) {
                try {
                    statment.close();
                } catch (SQLException e) {
                }
            }
        }
        return resultList;
    }

    /**
     * 构建Map类型查询.
     *
     * @param sql
     * @param conn
     * @return
     */
    public Map<String, Long> bulidMapTypeSelect(String sql, Connection conn) {

        Map<String, Long> resultMap = new HashMap<String, Long>();
        //参数验证
        if (Assert.isEmptyString(sql) == true || conn == null) {
            return resultMap;
        }
        Statement statment = null;
        ResultSet rs = null;
        try {
            statment = conn.createStatement();
            rs = statment.executeQuery(sql);
            while (rs.next()) {
                resultMap.put(String.valueOf(rs.getString(1)), rs.getLong(2));
            }
        } catch (Exception e) {
            theLogger.exception("sql:" + sql, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (statment != null) {
                try {
                    statment.close();
                } catch (SQLException e) {
                }
            }
        }
        return resultMap;
    }

    /**
     * 查询SQL取得结果.
     *
     * @param sql
     * @return
     */
    public List<String> selectRecords(String sql, Connection conn) {

        List<String> resulteData = new ArrayList<String>();
        if (Assert.isEmptyString(sql) == true || conn == null) {
            return resulteData;
        }
        Statement statment = null;
        ResultSet resultSet = null;
        try {
            statment = conn.createStatement();
            resultSet = statment.executeQuery(sql);
            while (resultSet.next()) {
                resulteData.add(String.valueOf(resultSet.getObject(1)));
            }
        } catch (Exception e) {
            theLogger.exception("sql:" + sql, e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                }
            }
            if (statment != null) {
                try {
                    statment.close();
                } catch (SQLException e) {
                }
            }
        }
        return resulteData;
    }

    /**
     * 查询SQL取得结果.
     *
     * @param sql
     * @return
     */
    public List<Object[]> selectRecords(String sql) {

        if (Assert.isEmptyString(sql) == true) {
            return null;
        }
        theLogger.debug("sql", sql);
        //
        Connection conn = null;
        Statement statment = null;
        ResultSet resultSet = null;
        List<Object[]> resulteData = new ArrayList<Object[]>();
        try {
            conn = this.getConnection();
            statment = conn.createStatement();
            resultSet = statment.executeQuery(sql);
            while (resultSet.next()) {
                Object[] rowData = new Object[resultSet.getMetaData().getColumnCount()];
                for (int c = 0; c < resultSet.getMetaData().getColumnCount(); c++) {
                    rowData[c] = resultSet.getObject(c + 1);
                }
                resulteData.add(rowData);
            }
        } catch (Exception e) {
            theLogger.exception("sql:" + sql, e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                }
            }
            if (statment != null) {
                try {
                    statment.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
        return resulteData;
    }

    /**
     * 执行写入SQL.
     *
     * @param sql
     * @return
     * @throws Exception
     */
    public int saveOrUpdateBySql(String sql) throws Exception {

        if (Assert.isEmptyString(sql) == true) {
            return 0;
        }
        theLogger.debug("sql", sql);
        //
        Connection conn = null;
        Statement statment = null;
        int runSqlRusult = 0;
        try {
            conn = this.getConnection();
            statment = conn.createStatement();
            runSqlRusult = statment.executeUpdate(sql);
        } catch (Exception e) {
            theLogger.exception("sql:" + sql, e);
        } finally {
            if (statment != null) {
                try {
                    statment.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
        return runSqlRusult;
    }
}
