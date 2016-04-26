package com.byw.stock.house.data.center.platform.services;

import com.byw.stock.house.platform.core.metrics.AbstractMetricMBean;
import com.byw.stock.house.platform.core.services.IPlatformService;
import com.byw.stock.house.platform.core.services.PlatformServiceHelper;
import com.byw.stock.house.platform.core.services.PlatformServiceInfo;
import com.byw.stock.house.platform.core.utils.Assert;
import com.byw.stock.house.platform.core.utils.IOUtils;
import com.byw.stock.house.platform.log.PlatformLogger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.management.DynamicMBean;
import java.util.*;


/**
 * 
 * 缓存服务，字典信息，权限点缓存.
 * 
 * 缓存服务，提供城市，行业，字典信息，权限点缓存..
 * 
 * @title CacheService
 * @package com.topsec.tss.platform.service
 * @author baiyanwei
 * @version
 * @date 2014-6-25
 * 
 */
@PlatformServiceInfo(description = "CacheService", configurationPath = "application/services/CacheService/")
public class CacheService extends AbstractMetricMBean implements IPlatformService, DynamicMBean {

    /**
     * Logger.
     */
    final private static PlatformLogger theLogger = PlatformLogger.getLogger(CacheService.class);

    /**
     * NAV配置文件路径.
     */
    final private static String NAV_DATA_PATH = "navdata/";
    /**
     * 静态导航树缓存.
     */
    private HashMap<String, JSONArray> _staticNavMap = new HashMap<String, JSONArray>();
    /**
     * 字典信息缓存.
     */
    private HashMap<String, JSONObject> _dictionaryCacheMap = new HashMap<String, JSONObject>();
    /**
     * 权限点缓存.
     */
    private HashMap<String, JSONObject> _autPermissionCacheMap = new HashMap<String, JSONObject>();
    /**
     * 权限点缓存ID与编码缓存.
     */
    private HashMap<String, String> _autPermissionID2KeyCacheMap = new HashMap<String, String>();

    @Override
    public void start() throws Exception {

        long currentPoint = System.currentTimeMillis();
        //#1 加载维度缓存
        loadDimeCache();
        //#2 加载权限点缓存
        loadAutPermissionCache();
        //#3 
        loadNavCach();
        //
        theLogger.info("startUp", String.valueOf(_dictionaryCacheMap.size()), String.valueOf(_autPermissionCacheMap.size()), String.valueOf(System.currentTimeMillis() - currentPoint));
    }

    @Override
    public void stop() throws Exception {

        _staticNavMap.clear();
        _dictionaryCacheMap.clear();
        _autPermissionCacheMap.clear();
        _autPermissionID2KeyCacheMap.clear();
    }

    /**
     * 根据树名取得对应的结构，如果树是第一次访问，自动从系统中加载入CACHE.
     * 
     * @param navName
     * @return
     */
    public JSONArray getStaticNavData(String navName) {

        if (Assert.isEmptyString(navName) == true) {
            return new JSONArray();
        }
        //
        if (this._staticNavMap.containsKey(navName) == true) {
            return this._staticNavMap.get(navName);
        }
        //加载树文件
        StringBuffer treeContent = loadStaticGuideTreeData(navName);

        JSONArray treeData = null;
        if (treeContent == null) {
            treeData = new JSONArray();
        } else {
            treeData = new JSONArray(treeContent.toString());
        }
        //
        synchronized (this._staticNavMap) {
            this._staticNavMap.put(navName, treeData);
        }
        return treeData;
    }

    /**
     * 取得权限点ID与编码对应的缓存集合.
     * 
     * @return
     */
    public HashMap<String, String> getAutPermissionID2KeyCacheMap() {

        return this._autPermissionID2KeyCacheMap;
    }

    /**
     * 根据字典类型取得对应的值范围.
     * 
     * @param dictionaryType
     * @return
     */
    public List<String[]> getDictionarySelectDataByType(String dictionaryType) {

        if (Assert.isEmptyString(dictionaryType) == true) {
            return new ArrayList<String[]>();
        }
        //
        ArrayList<String[]> selectDataList = new ArrayList<String[]>();
        //按字典类型取得一组对应的字典值
        for (Iterator<String> keyIter = _dictionaryCacheMap.keySet().iterator(); keyIter.hasNext();) {
            JSONObject valueObj = _dictionaryCacheMap.get(keyIter.next());
            if (valueObj == null) {
                continue;
            }
            //NAME,DICTIONARY_TYPE,VALUE,IS_DEFAULT,DESCRIPTION
            if (valueObj.has("DICTIONARY_TYPE") == true && valueObj.getString("DICTIONARY_TYPE").equalsIgnoreCase(dictionaryType) == true) {
                selectDataList.add(new String[] { valueObj.getString("NAME"), valueObj.getString("VALUE"), String.valueOf(valueObj.getBoolean("IS_DEFAULT")) });
            }
        }

        return selectDataList;
    }

    /**
     * 
     * 根据字段parentName值去查找.
     * 
     * @param parentName
     * @return
     */
    public List<String[]> getDictionarySelectDataByParentName(String dictionaryType, String parentName) {

        if (Assert.isEmptyString(parentName) == true) {
            return new ArrayList<String[]>();
        }
        //
        ArrayList<String[]> selectDataList = new ArrayList<String[]>();
        //按字典类型取得一组对应的字典值
        for (Iterator<String> keyIter = _dictionaryCacheMap.keySet().iterator(); keyIter.hasNext();) {
            JSONObject valueObj = _dictionaryCacheMap.get(keyIter.next());
            if (valueObj == null) {
                continue;
            }
            if (valueObj.has("DICTIONARY_TYPE") == false || valueObj.getString("DICTIONARY_TYPE").equalsIgnoreCase(dictionaryType) == false) {
                continue;
            }
            //NAME,DICTIONARY_TYPE,VALUE,IS_DEFAULT,DESCRIPTION
            if (valueObj.has("PARENT_NAME") == true && Assert.isEmptyString(valueObj.getString("PARENT_NAME")) == false && valueObj.getString("PARENT_NAME").equals(parentName) == true) {
                selectDataList.add(new String[] { valueObj.getString("NAME"), valueObj.getString("VALUE"), String.valueOf(valueObj.getBoolean("IS_DEFAULT")), valueObj.getString("PARENT_NAME") });
            }
        }

        return selectDataList;
    }

    /**
     * 根据字典值找到对应的字典名.
     * 
     * @param dictionaryType
     * @param value
     * @return
     */
    public String getDictionaryNameByDictionaryValue(String dictionaryType, String value) {

        if (Assert.isEmptyString(dictionaryType) == true || Assert.isEmptyString(value) == true) {
            return "";
        }

        for (Iterator<String> keyIter = _dictionaryCacheMap.keySet().iterator(); keyIter.hasNext();) {
            JSONObject valueObj = _dictionaryCacheMap.get(keyIter.next());
            if (valueObj == null) {
                continue;
            }
            //判断是否为要检查的值
            //NAME,DICTIONARY_TYPE,VALUE,IS_DEFAULT,DESCRIPTION
            if (valueObj.has("DICTIONARY_TYPE") == true && valueObj.getString("DICTIONARY_TYPE").equalsIgnoreCase(dictionaryType) == true) {
                if (valueObj.has("VALUE") == true && valueObj.getString("VALUE").equalsIgnoreCase(value) == true) {
                    return valueObj.getString("NAME");
                }
            }
        }

        return value;
    }

    /**
     * 根据权限点编码取得对应的权限点.
     * 
     * @param code
     * @return
     */
    public JSONObject getAutPermissionByCode(String code) {

        if (Assert.isEmptyString(code) == true) {
            return null;
        }
        return this._autPermissionCacheMap.get(code);
    }

    /**
     * 根据权限点ID取得对应的权限点编码.
     * 
     * @param permissionIdList
     * @return
     */
    public List<String> getPermissionPointCodeById(List<String> permissionIdList) {

        if (Assert.isEmptyCollection(permissionIdList) == true) {
            return new ArrayList<String>();
        }
        List<String> permissionPointCodeList = new ArrayList<String>();
        for (String permissionId : permissionIdList) {
            //从权限点快照中取得对应ID的权限点编码
            if (_autPermissionID2KeyCacheMap.containsKey(permissionId) == true) {
                permissionPointCodeList.add(_autPermissionID2KeyCacheMap.get(permissionId));
            }
        }
        return permissionPointCodeList;
    }

    /**
     * 根据父权限点取得其下子权限点集合.
     * 
     * @param code
     * @return
     */
    public List<JSONObject> getSonAutPermissionListByParentCode(String code) {

        if (Assert.isEmptyString(code) == true) {
            return null;
        }

        List<JSONObject> sonList = new ArrayList<JSONObject>();

        for (Iterator<String> keyIter = this._autPermissionCacheMap.keySet().iterator(); keyIter.hasNext();) {
            String keyName = keyIter.next();
            JSONObject autPermissionJson = this._autPermissionCacheMap.get(keyName);
            //对比JSON中的父编码是否为要求参数中的编码
            String parentCode = autPermissionJson.getString("PARENT_POINT_CODE");
            if (code.equalsIgnoreCase(parentCode) == true) {
                sonList.add(autPermissionJson);
            }
        }
        //排序
        if (sonList.isEmpty() == false) {
            Collections.sort(sonList, new Comparator<JSONObject>() {

                public int compare(JSONObject o1, JSONObject o2) {

                    return o1.getString("POINT_CODE").compareTo(o2.getString("POINT_CODE"));
                }
            });
        }

        return sonList;
    }

    /**
     * 加载维度缓存.
     * 
     */
    public void loadDimeCache() {

        //dim_city|dim_dictionary|dim_industry 
        //#1 加载字典信息缓存
        loadDimResourceAndPackage("ID", "ID,NAME,DICTIONARY_TYPE,VALUE,IS_DEFAULT,DESCRIPTION,PARENT_NAME".split(","), "sql.dictionary", this._dictionaryCacheMap);
    }

    /**
     * 加载NAV配置.
     * 
     */
    public void loadNavCach() {

        //  目前NAV是在访问的时动态加载的，这里只清除旧的即可
        synchronized (_staticNavMap) {
            _staticNavMap.clear();
        }
    }

    /**
     * 加载权限点缓存.
     * 
     */
    public void loadAutPermissionCache() {

        _autPermissionID2KeyCacheMap.clear();
        //#1 加载字典信息缓存
        loadDimResourceAndPackage("POINT_CODE", "ID,POINT_CODE,NAME,DESCRIPTION,IS_ACTIVE,PARENT_POINT_CODE,FULL_PATH".split(","), "sql.permission", this._autPermissionCacheMap);

        if (this._autPermissionCacheMap == null || this._autPermissionCacheMap.isEmpty() == true) {
            return;
        }
        //将权限点数据生成权限点ID与编码的快照
        for (Iterator<String> keyIter = this._autPermissionCacheMap.keySet().iterator(); keyIter.hasNext();) {
            String keyName = keyIter.next();
            JSONObject autPermissionJson = this._autPermissionCacheMap.get(keyName);
            //对比JSON中的父编码是否为要求参数中的编码
            long id = autPermissionJson.getLong("ID");
            _autPermissionID2KeyCacheMap.put(String.valueOf(id), keyName);
        }
    }

    /**
     * 加载static nav.
     * 
     * @param navdata
     * @return
     */
    private StringBuffer loadStaticGuideTreeData(String navdata) {

        String treeResourceName = NAV_DATA_PATH + navdata + ".js";
        //从文件流中加载对应的NAV数据文件
        return IOUtils.getInputStream2StringBuffer(this.getClass().getClassLoader().getResourceAsStream(treeResourceName));
    }

    /**
     * 从数据库加载维度信息到缓存中.
     * 
     */
    private void loadDimResourceAndPackage(String key, String[] feilds, String sql, HashMap<String, JSONObject> cacheMap) {

        //清理目标集合
        cacheMap.clear();
        //
        theLogger.debug(sql);
        //数据库存储服务
        DataBaseStorageService dataBaseStorageService = PlatformServiceHelper.findService(DataBaseStorageService.class);
        //在数据库查找对应SQL的数据
        List<Object[]> cacheDataList = dataBaseStorageService.selectRecords(theLogger.getMessageFormat(sql));
        //将SQL的RESULT集合封装成JSON对象
        List<JSONObject> cacheJSONDataList = packageRowToJSON(cacheDataList, feilds);
        //将数据填写到目标集合中
        fillCacheMap(key, cacheJSONDataList, cacheMap);
    }

    /**
     * 封装数据行成JSON对象.
     * 
     * @param rowList
     * @param names
     * @return
     */
    private List<JSONObject> packageRowToJSON(List<Object[]> rowList, String[] names) {

        if (rowList == null || rowList.isEmpty()) {
            return null;
        }
        if (names == null || names.length == 0) {
            return null;
        }
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        //按集合的列名封装对应的列数据
        for (Object[] rowArray : rowList) {
            //ID,NAME,DICTIONARY_TYPE,VALUE,IS_DEFAULT,DESCRIPTION
            int size = names.length;
            if (size > rowArray.length) {
                size = rowArray.length;
            }
            JSONObject rowObj = new JSONObject();
            for (int i = 0; i < size; i++) {
                rowObj.put(names[i], rowArray[i]);
            }
            jsonList.add(rowObj);
        }
        return jsonList;
    }

    /**
     * 根据KEY填冲缓存.
     * 
     * @param key
     * @param dataList
     * @param cacheMap
     */
    private void fillCacheMap(String key, List<JSONObject> dataList, HashMap<String, JSONObject> cacheMap) {

        synchronized (cacheMap) {
            if (dataList != null) {
                for (int i = 0; i < dataList.size(); i++) {
                    JSONObject rowObj = dataList.get(i);
                    if (rowObj.has(key) == false) {
                        continue;
                    }
                    Object valueObj = rowObj.get(key);
                    if (valueObj == null) {
                        continue;
                    }
                    String keyName = valueObj.toString();
                    cacheMap.put(keyName, rowObj);
                }
            }
        }
    }
}
